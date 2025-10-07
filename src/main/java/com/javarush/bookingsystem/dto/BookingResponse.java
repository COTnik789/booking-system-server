package com.javarush.bookingsystem.dto;


import java.time.LocalDateTime;


public record BookingResponse(
        Long id,
        Long roomId,
        String userName,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}