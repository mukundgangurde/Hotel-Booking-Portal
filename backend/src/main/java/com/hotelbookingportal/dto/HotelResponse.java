package com.hotelbookingportal.dto;

import java.util.List;

public record HotelResponse(
        Long id,
        String name,
        String location,
        String description,
        List<String> amenities,
        String imageUrl,
        List<RoomTypeResponse> roomTypes
) {
}
