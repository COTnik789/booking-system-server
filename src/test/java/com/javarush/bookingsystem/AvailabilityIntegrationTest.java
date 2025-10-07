package com.javarush.bookingsystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.bookingsystem.domain.Room;
import com.javarush.bookingsystem.dto.CreateBookingRequest;
import com.javarush.bookingsystem.repository.BookingRepository;
import com.javarush.bookingsystem.repository.RoomRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AvailabilityIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired BookingRepository bookingRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired ObjectMapper om;

    private Long roomId;

    @BeforeEach
    void init() {
        bookingRepository.deleteAll();
        bookingRepository.flush();

        roomRepository.deleteAll();
        roomRepository.flush();

        Room r = roomRepository.save(Room.builder()
                .name("R3")
                .openTime(LocalTime.of(8,0))
                .closeTime(LocalTime.of(20,0))
                .build());
        roomId = r.getId();
    }

    @Test
    void fullDayFree_flagTrue_whenNoBookings() throws Exception {
        LocalDate d = LocalDate.now().plusDays(1);

        String resp = mvc.perform(get("/api/rooms/{id}/availability", roomId)
                        .param("from", d.toString())
                        .param("to", d.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode arr = om.readTree(resp);
        assertTrue(arr.isArray());
        assertTrue(arr.size() == 1);
        assertTrue(arr.get(0).get("fullDayFree").asBoolean());
    }

    @Test
    void availability_splitsGaps_andMarksPartialFalse() throws Exception {
        LocalDate d = LocalDate.now().plusDays(1);
        // создаём бронь 10–11 => ожидаем окна 8–10 и 11–20
        var create = new CreateBookingRequest(roomId, "u", d.atTime(10,0), d.atTime(11,0));
        mvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(create)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/rooms/{id}/availability", roomId)
                        .param("from", d.toString())
                        .param("to", d.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].fullDayFree", is(false)))
                .andExpect(jsonPath("$[1].fullDayFree", is(false)))
                .andExpect(jsonPath("$[0].start", containsString("T08:00:00")))
                .andExpect(jsonPath("$[0].end", containsString("T10:00:00")))
                .andExpect(jsonPath("$[1].start", containsString("T11:00:00")))
                .andExpect(jsonPath("$[1].end", containsString("T20:00:00")));
    }

    @Test
    void availability_mixedRange_marksNextDayFullTrue() throws Exception {
        LocalDate d1 = LocalDate.now().plusDays(1);
        LocalDate d2 = d1.plusDays(1);

        // В первый день есть бронь, второй день пустой => ждём 2 интервала (день1 2 окна), и 1 интервал (день2 full)
        var create = new CreateBookingRequest(roomId, "u", d1.atTime(10,0), d1.atTime(11,0));
        mvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(create)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/rooms/{id}/availability", roomId)
                        .param("from", d1.toString())
                        .param("to", d2.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].fullDayFree", is(true)));
    }
}
