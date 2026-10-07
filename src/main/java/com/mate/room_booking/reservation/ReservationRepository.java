package com.mate.room_booking.reservation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByUserId(Integer userId);
}