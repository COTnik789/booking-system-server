package com.javarush.bookingsystem;

import com.javarush.bookingsystem.domain.Booking;
import com.javarush.bookingsystem.domain.Room;
import com.javarush.bookingsystem.dto.CreateBookingRequest;
import com.javarush.bookingsystem.repository.BookingRepository;
import com.javarush.bookingsystem.repository.RoomRepository;
import com.javarush.bookingsystem.service.BookingService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BookingServiceIntegrationTest {

    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired PlatformTransactionManager txm;

    private Room room;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        bookingRepository.flush();
        roomRepository.deleteAll();
        roomRepository.flush();
        room = roomRepository.save(Room.builder()
                .name("R1")
                .openTime(LocalTime.of(8,0))
                .closeTime(LocalTime.of(20,0))
                .build());
    }

    @Test
    void bookRoom_success() {
        LocalDate d = LocalDate.now().plusDays(1);
        var req = new CreateBookingRequest(
                room.getId(), "alice",
                d.atTime(10,0),
                d.atTime(11,0));
        Booking b = bookingService.bookRoom(req);
        assertNotNull(b.getId());
        assertEquals("alice", b.getUserName());
    }

    @Test
    void bookRoom_overlap_conflict() {
        LocalDate d = LocalDate.now().plusDays(1);
        bookingService.bookRoom(new CreateBookingRequest(
                room.getId(),"alice", d.atTime(10,0), d.atTime(11,0)));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                bookingService.bookRoom(new CreateBookingRequest(
                        room.getId(),"bob", d.atTime(10,30), d.atTime(11,30))));
        assertTrue(ex.getMessage().toLowerCase().contains("указанный период"));
    }

    @Test
    void bookRoom_crossDay_rejected() {
        LocalDate d = LocalDate.now().plusDays(1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookingService.bookRoom(new CreateBookingRequest(
                        room.getId(),"alice",
                        d.atTime(19,0),
                        d.plusDays(1).atTime(9,0))));
        assertTrue(ex.getMessage().toLowerCase().contains("один рабочий день")
                || ex.getMessage().toLowerCase().contains("пересекать сутки"));
    }

    @Test
    void bookRoom_past_rejected() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookingService.bookRoom(new CreateBookingRequest(
                        room.getId(),"alice", start, end)));
        assertTrue(ex.getMessage().toLowerCase().contains("уже прошло"));
    }

    @Test
    void concurrentBooking_onlyOneSucceeds() throws InterruptedException {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime start = date.atTime(10,0);
        LocalDateTime exit = date.atTime(11,0);

        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger(0);
        Runnable task = () -> {
            try {
                startGate.await();
                bookingService.bookRoom(new CreateBookingRequest(room.getId(),"u", start, exit));
                success.incrementAndGet();
            } catch (Exception ignored) {}
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start(); t2.start();
        startGate.countDown();
        t1.join(); t2.join();

        TransactionTemplate tt = new TransactionTemplate(txm);
        Long count = tt.execute(s1 -> bookingRepository.count());
        assertEquals(1L, count);
        assertEquals(1, success.get());
    }
}
