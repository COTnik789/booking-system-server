package com.javarush.bookingsystem.service;


import com.javarush.bookingsystem.domain.Booking;
import com.javarush.bookingsystem.domain.Room;
import com.javarush.bookingsystem.dto.CreateBookingRequest;
import com.javarush.bookingsystem.dto.TimeIntervalDto;
import com.javarush.bookingsystem.repository.BookingRepository;
import com.javarush.bookingsystem.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


@Service
public class BookingService {


    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;


    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }


    @Transactional
    public Booking bookRoom(CreateBookingRequest req) {
        validateRequest(req);


        Room room = roomRepository.findByIdForUpdate(req.roomId())
                .orElseThrow(() -> new IllegalArgumentException("Комната с указанным ID не найдена"));

        validateWorkingHours(req.startTime(), req.endTime(), room);


        if (!bookingRepository.findOverlapsForUpdate(room.getId(), req.startTime(), req.endTime()).isEmpty()) {
            throw new IllegalStateException("Комната уже забронирована на указанный период");
        }


        Booking booking = Booking.builder()
                .room(room)
                .userName(req.userName())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .build();


        return bookingRepository.save(booking);
    }


    @Transactional
    public void deleteBooking(Long id, Optional<String> requester) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Бронирование не найдено"));

        if (requester.isEmpty()) {
            throw new SecurityException("Удалять бронь может только автор — укажите заголовок X-User");
        }

        String user = requester.get();
        if (!user.equalsIgnoreCase(b.getUserName())) {
            throw new SecurityException("Удалять бронь может только её автор");
        }

        bookingRepository.delete(b);
    }



    @Transactional(readOnly = true)
    public List<Booking> getBookingsForRoom(Long roomId) {
        return bookingRepository.findByRoomIdOrderByStartTime(roomId);
    }


    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {return bookingRepository.findAll();
}


@Transactional(readOnly = true)
public List<Booking> getBookings(LocalDateTime from, LocalDateTime to, Long roomId) {
    if (!from.isBefore(to)) {
        throw new IllegalArgumentException("Параметр 'from' должен быть раньше 'to'");
    }
    return bookingRepository.findInRange(from, to, roomId);
}


@Transactional(readOnly = true)
public List<TimeIntervalDto> getAvailability(Long roomId, LocalDate from, LocalDate to) {
    Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Комната с указанным ID не найдена"));


    List<TimeIntervalDto> free = new ArrayList<>();
    LocalDate cursor = from;
    while (!cursor.isAfter(to)) {
        LocalDateTime dayStart = cursor.atTime(room.getOpenTime());
        LocalDateTime dayEnd = cursor.atTime(room.getCloseTime());

        List<Booking> booked = bookingRepository.findOverlaps(roomId, dayStart, dayEnd);
        booked.sort(Comparator.comparing(Booking::getStartTime));

        if (booked.isEmpty()) {
            free.add(new TimeIntervalDto(dayStart, dayEnd, true));
            cursor = cursor.plusDays(1);
            continue;
        }

        LocalDateTime current = dayStart;
        for (Booking b : booked) {
            if (b.getStartTime().isAfter(current)) {
                free.add(new TimeIntervalDto(current, min(b.getStartTime(), dayEnd), false));
            }
            if (b.getEndTime().isAfter(current)) {
                current = b.getEndTime();
            }
            if (!current.isBefore(dayEnd)) break;
        }

        if (current.isBefore(dayEnd)) {
            free.add(new TimeIntervalDto(current, dayEnd, false));
        }

        cursor = cursor.plusDays(1);
    }
    return free;
}


private void validateRequest(CreateBookingRequest req) {
    if (!req.startTime().isBefore(req.endTime())) {
        throw new IllegalArgumentException("Время начала должно быть раньше времени окончания");
    }
    if (req.startTime().toLocalDate().isAfter(req.endTime().toLocalDate())) {
        throw new IllegalArgumentException("Бронирование не может пересекать сутки");
    }
    if (req.startTime().isBefore(LocalDateTime.now())) {
        throw new IllegalArgumentException("Нельзя бронировать время, которое уже прошло");
    }
}


private void validateWorkingHours(LocalDateTime start, LocalDateTime end, Room room) {
    LocalTime s = start.toLocalTime();
    LocalTime e = end.toLocalTime();
    if (s.isBefore(room.getOpenTime()) || e.isAfter(room.getCloseTime())) {
        throw new IllegalArgumentException("Время бронирования выходит за пределы рабочего времени комнаты");
    }
    if (!start.toLocalDate().equals(end.toLocalDate())) {
        throw new IllegalArgumentException("Бронирование должно укладываться в один рабочий день");
    }
}


private static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
    return a.isBefore(b) ? a : b;
}
}