package com.hotelbookingportal.controller;

import com.hotelbookingportal.dto.BookingQuoteResponse;
import com.hotelbookingportal.dto.BookingRequest;
import com.hotelbookingportal.dto.BookingResponse;
import com.hotelbookingportal.dto.QuoteRequest;
import com.hotelbookingportal.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings/quote")
    public BookingQuoteResponse quote(@Valid @RequestBody QuoteRequest request) {
        return bookingService.quote(request);
    }

    @PostMapping("/bookings")
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/bookings/{reference}")
    public BookingResponse getBooking(
            @PathVariable String reference,
            @RequestParam String email
    ) {
        return bookingService.getBooking(reference, email);
    }

    @DeleteMapping("/bookings/{reference}")
    public BookingResponse cancelBooking(
            @PathVariable String reference,
            @RequestParam String email
    ) {
        return bookingService.cancelBooking(reference, email);
    }
}
