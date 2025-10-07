package com.javarush.bookingsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.bookingsystem.domain.Room;
import com.javarush.bookingsystem.repository.BookingRepository;
import com.javarush.bookingsystem.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoomControllerDtoIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Autowired BookingRepository bookingRepository;
    @Autowired RoomRepository roomRepository;

    @BeforeEach
    void clean() {
        bookingRepository.deleteAll();
        bookingRepository.flush();

        roomRepository.deleteAll();
        roomRepository.flush();
    }

    @Test
    void postAndGet_returnsRoomResponseDto() throws Exception {
        String body = """
                {"name":"тестовая комната","openTime":"08:00:00","closeTime":"20:00:00"}
                """;

        String created = mvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("тестовая комната")))
                .andExpect(jsonPath("$.openTime", is("08:00:00")))
                .andExpect(jsonPath("$.closeTime", is("20:00:00")))
                .andReturn().getResponse().getContentAsString();

        long id = om.readTree(created).get("id").asLong();

        mvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is((int) id)))
                .andExpect(jsonPath("$[0].name", is("тестовая комната")));

        Room r = roomRepository.findById(id).orElseThrow();
        assertEquals(LocalTime.of(8,0), r.getOpenTime());
        assertEquals(LocalTime.of(20,0), r.getCloseTime());
    }
}
