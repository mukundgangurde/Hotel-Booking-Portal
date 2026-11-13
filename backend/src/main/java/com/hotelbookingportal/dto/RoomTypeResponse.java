package com.hotelbookingportal.dto;

import java.math.BigDecimal;

public record RoomTypeResponse(
        Long id,
        String roomName,
        BigDecimal pricePerNight,
        int totalRooms,
        Integer availableRooms
) {
}
