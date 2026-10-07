package com.mate.room_booking.reservation;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        Integer userId,
        Integer roomId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String purpose
) {
}