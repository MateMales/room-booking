package com.mate.room_booking.reservation;

import java.util.List;

import com.mate.room_booking.common.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> findAll(Integer userId) {
        if (userId == null) {
            return reservationRepository.findAll();
        }
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> findByUser(Integer userId) {
        return reservationRepository.findByUserId(userId);
    }

    public Reservation create(Integer userId, CreateReservationRequest request) {
        Reservation reservation = new Reservation(
                userId,
                request.roomId(),
                request.startTime(),
                request.endTime(),
                request.purpose());
        return reservationRepository.save(reservation);
    }

    public Reservation cancel(Integer reservationId, Integer currentUserId, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation " + reservationId + " not found"));

        if (!isAdmin && !reservation.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You can only cancel your own reservations");
        }

        reservation.cancel();
        return reservationRepository.save(reservation);
    }
}