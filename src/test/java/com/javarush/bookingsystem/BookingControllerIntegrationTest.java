package com.javarush.bookingsystem;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingControllerIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired RoomRepository roomRepository;
    @Autowired BookingRepository bookingRepository;
    @Autowired ObjectMapper om;

    private Long roomId;

    @BeforeEach
    void init() {
        bookingRepository.deleteAll();
        bookingRepository.flush();

        roomRepository.deleteAll();
        roomRepository.flush();

        Room r = roomRepository.save(Room.builder()
                .name("R2")
                .openTime(LocalTime.of(8,0))
                .closeTime(LocalTime.of(20,0))
                .build());
        roomId = r.getId();
    }

    @Test
    void postBooking_usesXUserAndReturnsDto() throws Exception {
        var d = LocalDate.now().plusDays(1);
        var req = new CreateBookingRequest(roomId, "ignored", d.atTime(10,0), d.atTime(11,0));

        mvc.perform(post("/api/bookings")
                        .header("X-User", "ivan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.roomId", is(roomId.intValue())))
                .andExpect(jsonPath("$.userName", is("ivan")))
                .andExpect(jsonPath("$.startTime", containsString("T10:00:00")))
                .andExpect(jsonPath("$.endTime", containsString("T11:00:00")));
    }

    @Test
    void deleteBooking_forbiddenWithoutXUser_orWrongUser() throws Exception {
        var d = LocalDate.now().plusDays(1);
        // создаём бронь от имени alice
        var create = new CreateBookingRequest(roomId, "alice", d.atTime(10,0), d.atTime(11,0));
        String id = mvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long bookingId = om.readTree(id).get("id").asLong();

        // без X-User -> 403
        mvc.perform(delete("/api/bookings/{id}", bookingId))
                .andExpect(status().isForbidden());

        // с другим пользователем -> 403
        mvc.perform(delete("/api/bookings/{id}", bookingId)
                        .header("X-User", "bob"))
                .andExpect(status().isForbidden());

        // с правильным пользователем -> 204
        mvc.perform(delete("/api/bookings/{id}", bookingId)
                        .header("X-User", "alice"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getBookingsRoom_returnsDtoArray() throws Exception {
        var d = LocalDate.now().plusDays(1);
        var a = new CreateBookingRequest(roomId, "u", d.atTime(9,0), d.atTime(10,0));
        mvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(a)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/bookings/room/{roomId}", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomId", is(roomId.intValue())))
                .andExpect(jsonPath("$[0].userName", is("u")));
    }
}