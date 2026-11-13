package com.hotelbookingportal.dto;

import com.hotelbookingportal.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        String bookingReference,
        Long hotelId,
        String hotelName,
        Long roomTypeId,
        String roomName,
        String customerName,
        String email,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal totalAmount,
        BookingStatus status,
        LocalDateTime createdAt
) {
}
