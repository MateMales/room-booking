package com.mate.room_booking.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ReservationTest {

    private Reservation newReservation() {
        return new Reservation(1, 3,
                LocalDateTime.of(2030, 1, 15, 10, 0),
                LocalDateTime.of(2030, 1, 15, 12, 0),
                "Test");
    }

    @Test
    void newReservation_isActive() {
        Reservation reservation = newReservation();

        assertThat(reservation.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void cancel_setsStatusToCancelled() {
        Reservation reservation = newReservation();
        reservation.cancel();
        assertThat(reservation.getStatus()).isEqualTo("CANCELLED");
    }
}