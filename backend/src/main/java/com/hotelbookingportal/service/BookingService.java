package com.hotelbookingportal.service;

import com.hotelbookingportal.dto.BookingQuoteResponse;
import com.hotelbookingportal.dto.BookingRequest;
import com.hotelbookingportal.dto.BookingResponse;
import com.hotelbookingportal.dto.HotelResponse;
import com.hotelbookingportal.dto.QuoteRequest;
import com.hotelbookingportal.dto.RoomTypeResponse;
import com.hotelbookingportal.entity.Booking;
import com.hotelbookingportal.entity.BookingStatus;
import com.hotelbookingportal.entity.Hotel;
import com.hotelbookingportal.entity.RoomType;
import com.hotelbookingportal.exception.BadRequestException;
import com.hotelbookingportal.exception.ConflictException;
import com.hotelbookingportal.exception.ResourceNotFoundException;
import com.hotelbookingportal.repository.BookingRepository;
import com.hotelbookingportal.repository.HotelRepository;
import com.hotelbookingportal.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class BookingService {
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;

    public BookingService(
            HotelRepository hotelRepository,
            RoomTypeRepository roomTypeRepository,
            BookingRepository bookingRepository
    ) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> findHotels(String location, LocalDate checkIn, LocalDate checkOut) {
        validateOptionalDateRange(checkIn, checkOut);
        List<Hotel> hotels = location == null || location.isBlank()
                ? hotelRepository.findAll()
                : hotelRepository.findByLocationContainingIgnoreCase(location.trim());
        return hotels.stream()
                .map(hotel -> toHotelResponse(hotel, checkIn, checkOut))
                .toList();
    }

    @Transactional(readOnly = true)
    public HotelResponse getHotel(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        validateOptionalDateRange(checkIn, checkOut);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
        return toHotelResponse(hotel, checkIn, checkOut);
    }

    @Transactional(readOnly = true)
    public BookingQuoteResponse quote(QuoteRequest request) {
        validateDateRange(request.checkIn(), request.checkOut());
        RoomType roomType = getRoomType(request.roomTypeId());
        int availableRooms = getAvailableRooms(roomType, request.checkIn(), request.checkOut());
        if (availableRooms == 0) {
            throw new ConflictException("No rooms are available for the selected dates");
        }
        return toQuote(roomType, request.checkIn(), request.checkOut(), availableRooms);
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        validateDateRange(request.checkIn(), request.checkOut());
        RoomType roomType = roomTypeRepository.findByIdForUpdate(request.roomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found"));

        int availableRooms = getAvailableRooms(roomType, request.checkIn(), request.checkOut());
        if (availableRooms == 0) {
            throw new ConflictException("No rooms are available for the selected dates");
        }

        Booking booking = new Booking();
        booking.setBookingReference("HB-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase());
        booking.setRoomType(roomType);
        booking.setCustomerName(request.customerName().trim());
        booking.setEmail(request.email().trim());
        booking.setCheckIn(request.checkIn());
        booking.setCheckOut(request.checkOut());
        booking.setTotalAmount(calculateTotal(roomType.getPricePerNight(),
                request.checkIn(), request.checkOut()));
        booking.setStatus(BookingStatus.CONFIRMED);
        return toBookingResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(String reference, String email) {
        Booking booking = bookingRepository.findByBookingReferenceAndEmailIgnoreCase(reference, email)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return toBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(String reference, String email) {
        Booking booking = bookingRepository.findForUpdate(reference, email)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking = bookingRepository.save(booking);
        }
        return toBookingResponse(booking);
    }

    private HotelResponse toHotelResponse(Hotel hotel, LocalDate checkIn, LocalDate checkOut) {
        List<RoomTypeResponse> roomTypes = roomTypeRepository.findByHotelId(hotel.getId()).stream()
                .map(roomType -> new RoomTypeResponse(
                        roomType.getId(),
                        roomType.getRoomName(),
                        roomType.getPricePerNight(),
                        roomType.getTotalRooms(),
                        checkIn == null ? null : getAvailableRooms(roomType, checkIn, checkOut)
                ))
                .toList();

        List<String> amenities = hotel.getAmenities() == null || hotel.getAmenities().isBlank()
                ? List.of()
                : java.util.Arrays.stream(hotel.getAmenities().split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();

        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getLocation(),
                hotel.getDescription(),
                amenities,
                hotel.getImageUrl(),
                roomTypes
        );
    }

    private BookingQuoteResponse toQuote(
            RoomType roomType,
            LocalDate checkIn,
            LocalDate checkOut,
            int availableRooms
    ) {
        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        return new BookingQuoteResponse(
                roomType.getId(),
                checkIn,
                checkOut,
                nights,
                roomType.getPricePerNight(),
                roomType.getPricePerNight().multiply(BigDecimal.valueOf(nights)),
                availableRooms
        );
    }

    private BigDecimal calculateTotal(BigDecimal pricePerNight, LocalDate checkIn, LocalDate checkOut) {
        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        return pricePerNight.multiply(BigDecimal.valueOf(nights));
    }

    private int getAvailableRooms(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> overlappingBookings =
                bookingRepository.findByRoomTypeIdAndStatusAndCheckInLessThanAndCheckOutGreaterThan(
                        roomType.getId(),
                        BookingStatus.CONFIRMED,
                        checkOut,
                        checkIn
                );
        TreeMap<LocalDate, Integer> occupancyChanges = new TreeMap<>();
        occupancyChanges.put(checkIn, 0);
        int peakOccupancy = 0;
        for (Booking booking : overlappingBookings) {
            LocalDate occupiedFrom = booking.getCheckIn().isAfter(checkIn)
                    ? booking.getCheckIn()
                    : checkIn;
            LocalDate occupiedUntil = booking.getCheckOut().isBefore(checkOut)
                    ? booking.getCheckOut()
                    : checkOut;
            occupancyChanges.merge(occupiedFrom, 1, Integer::sum);
            occupancyChanges.merge(occupiedUntil, -1, Integer::sum);
        }
        int occupancy = 0;
        for (int change : occupancyChanges.values()) {
            occupancy += change;
            peakOccupancy = Math.max(peakOccupancy, occupancy);
        }
        return Math.max(0, roomType.getTotalRooms() - peakOccupancy);
    }

    private RoomType getRoomType(Long roomTypeId) {
        return roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found"));
    }

    private BookingResponse toBookingResponse(Booking booking) {
        RoomType roomType = booking.getRoomType();
        Hotel hotel = roomType.getHotel();
        return new BookingResponse(
                booking.getBookingReference(),
                hotel.getId(),
                hotel.getName(),
                roomType.getId(),
                roomType.getRoomName(),
                booking.getCustomerName(),
                booking.getEmail(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }

    private void validateOptionalDateRange(LocalDate checkIn, LocalDate checkOut) {
        if ((checkIn == null) != (checkOut == null)) {
            throw new BadRequestException("Both checkIn and checkOut must be provided together");
        }
        if (checkIn != null) {
            validateDateRange(checkIn, checkOut);
        }
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BadRequestException("Check-in and check-out dates are required");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BadRequestException("Check-in cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Check-out must be after check-in");
        }
    }
}
