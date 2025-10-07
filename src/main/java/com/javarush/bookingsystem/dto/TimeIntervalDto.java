package com.javarush.bookingsystem.dto;

import java.time.LocalDateTime;

public record TimeIntervalDto(
        LocalDateTime start,
        LocalDateTime end,
        boolean fullDayFree
) {}
