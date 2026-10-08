package com.mate.room_booking.reservation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        @NotNull Integer userId,
        @NotNull Integer roomId,
        @NotNull @Future LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @Size(max=200) String purpose
) {
}