package com.hotelbookingportal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record QuoteRequest(
        @NotNull @Positive Long roomTypeId,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut
) {
}
