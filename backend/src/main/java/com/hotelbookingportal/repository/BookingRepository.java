package com.hotelbookingportal.repository;

import com.hotelbookingportal.entity.Booking;
import com.hotelbookingportal.entity.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    List<Booking> findByRoomTypeIdAndStatusAndCheckInLessThanAndCheckOutGreaterThan(
            Long roomTypeId,
            BookingStatus status,
            LocalDate checkOut,
            LocalDate checkIn
    );

    Optional<Booking> findByBookingReferenceAndEmailIgnoreCase(
            String bookingReference,
            String email
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.bookingReference = :reference and lower(b.email) = lower(:email)")
    Optional<Booking> findForUpdate(
            @Param("reference") String reference,
            @Param("email") String email
    );

}
