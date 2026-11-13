package com.hotelbookingportal.repository;

import com.hotelbookingportal.entity.RoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository
        extends JpaRepository<RoomType, Long> {
    List<RoomType> findByHotelId(Long hotelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rt from RoomType rt where rt.id = :id")
    Optional<RoomType> findByIdForUpdate(@Param("id") Long id);
}
