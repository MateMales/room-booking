package com.mate.room_booking.reservation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepository;

    public ReservationController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @GetMapping
    public List<Reservation> getReservations(@RequestParam(required = false) Integer userId) {
        if (userId == null) {
            return reservationRepository.findAll();
        }
        return reservationRepository.findByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reservation createReservation(@RequestBody CreateReservationRequest request) {
        Reservation reservation = new Reservation(
                request.userId(),
                request.roomId(),
                request.startTime(),
                request.endTime(),
                request.purpose()
        );
        return reservationRepository.save(reservation);
    }
    @PatchMapping("/{id}/cancel")
    public Reservation cancelReservation(@PathVariable Integer id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow();
        reservation.cancel();
        return reservationRepository.save(reservation);
    }
}