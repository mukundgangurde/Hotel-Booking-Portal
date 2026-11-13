package com.hotelbookingportal.config;

import com.hotelbookingportal.entity.Hotel;
import com.hotelbookingportal.entity.RoomType;
import com.hotelbookingportal.repository.HotelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class SampleDataConfig {

    @Bean
    CommandLineRunner seedHotels(HotelRepository hotelRepository) {
        return args -> {
            if (hotelRepository.count() > 0) {
                return;
            }

            hotelRepository.saveAll(List.of(
                    hotel("The Harbour House", "Mumbai",
                            "A calm city stay close to the waterfront and local dining.",
                            "Wi-Fi,Restaurant,Air conditioning", "Standard Room", "6500.00", 8),
                    hotel("Garden Court", "Pune",
                            "A garden-side retreat with convenient access to the city.",
                            "Wi-Fi,Pool,Parking", "Queen Room", "4800.00", 6),
                    hotel("Lakeview Residency", "Udaipur",
                            "A relaxed base for exploring the lakes and old town.",
                            "Wi-Fi,Lake view,Breakfast", "Lake Room", "7200.00", 5),
                    hotel("The Southern Pearl", "Chennai",
                            "A comfortable stay with easy access to beaches and business districts.",
                            "Wi-Fi,Restaurant,Gym", "Deluxe Room", "5600.00", 7),
                    hotel("Himalayan Fern", "Manali",
                            "A mountain lodge designed for a quiet and scenic getaway.",
                            "Wi-Fi,Mountain view,Heating", "Mountain Room", "5900.00", 4)
            ));
        };
    }

    private Hotel hotel(
            String name,
            String location,
            String description,
            String amenities,
            String roomName,
            String price,
            int totalRooms
    ) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setLocation(location);
        hotel.setDescription(description);
        hotel.setAmenities(amenities);

        RoomType roomType = new RoomType();
        roomType.setHotel(hotel);
        roomType.setRoomName(roomName);
        roomType.setPricePerNight(new BigDecimal(price));
        roomType.setTotalRooms(totalRooms);
        hotel.setRoomTypes(List.of(roomType));
        return hotel;
    }
}
