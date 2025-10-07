package com.javarush.bookingsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record RoomResponse(
        Long id,
        String name,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        LocalTime openTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        LocalTime closeTime
) {
        public static RoomResponse from(com.javarush.bookingsystem.domain.Room r) {
                return new RoomResponse(r.getId(), r.getName(), r.getOpenTime(), r.getCloseTime());
        }
}
