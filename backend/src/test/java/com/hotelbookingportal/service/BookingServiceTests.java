package com.hotelbookingportal.service;

import com.hotelbookingportal.dto.BookingQuoteResponse;
import com.hotelbookingportal.dto.QuoteRequest;
import com.hotelbookingportal.entity.Booking;
import com.hotelbookingportal.entity.BookingStatus;
import com.hotelbookingportal.entity.Hotel;
import com.hotelbookingportal.entity.RoomType;
import com.hotelbookingportal.exception.BadRequestException;
import com.hotelbookingportal.exception.ConflictException;
import com.hotelbookingportal.repository.BookingRepository;
import com.hotelbookingportal.repository.HotelRepository;
import com.hotelbookingportal.repository.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private RoomTypeRepository roomTypeRepository;
    @Mock
    private BookingRepository bookingRepository;

    private BookingService bookingService;
    private RoomType roomType;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(hotelRepository, roomTypeRepository, bookingRepository);
        Hotel hotel = new Hotel();
        hotel.setId(8L);
        hotel.setName("Test Hotel");

        roomType = new RoomType();
        roomType.setId(4L);
        roomType.setHotel(hotel);
        roomType.setRoomName("Standard");
        roomType.setPricePerNight(new BigDecimal("125.50"));
        roomType.setTotalRooms(3);
    }

    @Test
    void quoteCalculatesNightsAndTotalOnTheServer() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        when(roomTypeRepository.findById(4L)).thenReturn(Optional.of(roomType));
        when(bookingRepository.findByRoomTypeIdAndStatusAndCheckInLessThanAndCheckOutGreaterThan(
                4L, BookingStatus.CONFIRMED, checkIn.plusDays(3), checkIn))
                .thenReturn(List.of());

        BookingQuoteResponse quote = bookingService.quote(
                new QuoteRequest(4L, checkIn, checkIn.plusDays(3)));

        assertEquals(3, quote.nights());
        assertEquals(new BigDecimal("376.50"), quote.totalAmount());
        assertEquals(3, quote.availableRooms());
    }

    @Test
    void quoteUsesPeakOverlappingOccupancyAcrossTheEntireStay() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        when(roomTypeRepository.findById(4L)).thenReturn(Optional.of(roomType));
        when(bookingRepository.findByRoomTypeIdAndStatusAndCheckInLessThanAndCheckOutGreaterThan(
                4L, BookingStatus.CONFIRMED, checkIn.plusDays(3), checkIn))
                .thenReturn(List.of(
                        booking(checkIn, checkIn.plusDays(2)),
                        booking(checkIn.plusDays(1), checkIn.plusDays(3))
                ));

        BookingQuoteResponse quote = bookingService.quote(
                new QuoteRequest(4L, checkIn, checkIn.plusDays(3)));

        assertEquals(1, quote.availableRooms());
    }

    @Test
    void quoteRejectsAStayWhenNoRoomIsAvailableForAtLeastOneNight() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        when(roomTypeRepository.findById(4L)).thenReturn(Optional.of(roomType));
        when(bookingRepository.findByRoomTypeIdAndStatusAndCheckInLessThanAndCheckOutGreaterThan(
                4L, BookingStatus.CONFIRMED, checkIn.plusDays(3), checkIn))
                .thenReturn(List.of(
                        booking(checkIn, checkIn.plusDays(2)),
                        booking(checkIn.plusDays(1), checkIn.plusDays(3)),
                        booking(checkIn.plusDays(1), checkIn.plusDays(2))
                ));

        assertThrows(ConflictException.class, () -> bookingService.quote(
                new QuoteRequest(4L, checkIn, checkIn.plusDays(3))));
    }

    @Test
    void rejectsCheckOutThatIsNotAfterCheckIn() {
        LocalDate checkIn = LocalDate.now().plusDays(1);

        assertThrows(BadRequestException.class, () -> bookingService.quote(
                new QuoteRequest(4L, checkIn, checkIn)));

        verify(roomTypeRepository, org.mockito.Mockito.never()).findById(any());
    }

    private Booking booking(LocalDate checkIn, LocalDate checkOut) {
        Booking booking = new Booking();
        booking.setRoomType(roomType);
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkOut);
        booking.setStatus(BookingStatus.CONFIRMED);
        return booking;
    }
}
