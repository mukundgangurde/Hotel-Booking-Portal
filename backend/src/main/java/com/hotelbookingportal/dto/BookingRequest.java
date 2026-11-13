package com.hotelbookingportal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record BookingRequest(
        @NotNull @Positive Long roomTypeId,
        @NotBlank String customerName,
        @NotBlank @Email String email,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut
) {
}
