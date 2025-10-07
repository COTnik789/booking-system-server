package com.javarush.bookingsystem.controller;
import com.javarush.bookingsystem.domain.Booking;
import com.javarush.bookingsystem.dto.BookingResponse;
import com.javarush.bookingsystem.dto.CreateBookingRequest;
import com.javarush.bookingsystem.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {


    private final BookingService bookingService;


    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    @PostMapping
    public ResponseEntity<BookingResponse> bookRoom(
            @RequestHeader(value = "X-User", required = false) String xUser,
            @Valid @RequestBody CreateBookingRequest request) {

        String decoded = (xUser != null) ? URLDecoder.decode(xUser, StandardCharsets.UTF_8) : null;

        CreateBookingRequest effective = (decoded != null && !decoded.isBlank())
                ? new CreateBookingRequest(request.roomId(), decoded, request.startTime(), request.endTime())
                : request;

        Booking saved = bookingService.bookRoom(effective);
        URI location = URI.create("/api/bookings/" + saved.getId());
        BookingResponse body = new BookingResponse(
                saved.getId(),
                saved.getRoom().getId(),
                saved.getUserName(),
                saved.getStartTime(),
                saved.getEndTime()
        );
        return ResponseEntity.created(location).body(body);
    }



    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<BookingResponse>> getBookingsForRoom(@PathVariable Long roomId) {
        List<BookingResponse> result = bookingService.getBookingsForRoom(roomId)
                .stream()
                .map(b -> new BookingResponse(
                        b.getId(),
                        b.getRoom().getId(),
                        b.getUserName(),
                        b.getStartTime(),
                        b.getEndTime()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }



    @GetMapping
    public ResponseEntity<List<BookingResponse>> getBookings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long roomId) {

        List<BookingResponse> result = bookingService.getBookings(from, to, roomId).stream()
                .map(b -> new BookingResponse(
                        b.getId(),
                        b.getRoom().getId(),
                        b.getUserName(),
                        b.getStartTime(),
                        b.getEndTime()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id,
                                              @RequestHeader(value = "X-User", required = false) String xUser) {
        bookingService.deleteBooking(id, Optional.ofNullable(xUser));
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/all")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> result = bookingService.getAllBookings().stream()
                .map(b -> new BookingResponse(
                        b.getId(),
                        b.getRoom().getId(),
                        b.getUserName(),
                        b.getStartTime(),
                        b.getEndTime()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
}