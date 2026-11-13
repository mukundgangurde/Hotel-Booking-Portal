package com.hotelbookingportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingQuoteResponse(
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        long nights,
        BigDecimal pricePerNight,
        BigDecimal totalAmount,
        int availableRooms
) {
}
