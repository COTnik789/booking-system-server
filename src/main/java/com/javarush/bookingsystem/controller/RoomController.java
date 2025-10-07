package com.javarush.bookingsystem.controller;


import com.javarush.bookingsystem.domain.Room;
import com.javarush.bookingsystem.dto.RoomRequest;
import com.javarush.bookingsystem.dto.RoomResponse;
import com.javarush.bookingsystem.dto.TimeIntervalDto;
import com.javarush.bookingsystem.service.BookingService;
import com.javarush.bookingsystem.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/rooms")
public class RoomController {


    private final RoomService roomService;
    private final BookingService bookingService;


    public RoomController(RoomService roomService, BookingService bookingService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
    }


    @PostMapping
    public ResponseEntity<RoomResponse> addRoom(@Valid @RequestBody RoomRequest req) {
        var room = com.javarush.bookingsystem.domain.Room.builder()
                .name(req.name())
                .openTime(req.openTime())
                .closeTime(req.closeTime())
                .build();

        var saved = roomService.addRoom(room);
        var location = URI.create("/api/rooms/" + saved.getId());
        return ResponseEntity.created(location).body(RoomResponse.from(saved));
    }


    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        var list = roomService.getAllRooms().stream()
                .map(RoomResponse::from)
                .toList();
        return ResponseEntity.ok(list);
    }



    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id)
                .map(RoomResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @GetMapping("/{id}/availability")
    public ResponseEntity<List<TimeIntervalDto>> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (to.isBefore(from)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bookingService.getAvailability(id, from, to));
    }
}