package com.hotelbookingportal.controller;

import com.hotelbookingportal.dto.HotelResponse;
import com.hotelbookingportal.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {
    private final BookingService bookingService;

    public HotelController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<HotelResponse> findHotels(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate checkOut
    ) {
        return bookingService.findHotels(location, checkIn, checkOut);
    }

    @GetMapping("/{hotelId}")
    public HotelResponse getHotel(
            @PathVariable Long hotelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate checkOut
    ) {
        return bookingService.getHotel(hotelId, checkIn, checkOut);
    }
}
