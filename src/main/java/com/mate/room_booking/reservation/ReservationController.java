package com.mate.room_booking.reservation;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // samo ADMIN (pravilo u SecurityConfig)
    @GetMapping
    public List<Reservation> getReservations(@RequestParam(required = false) Integer userId) {
        return reservationService.findAll(userId);
    }

    // rezervacije ulogovanog korisnika
    @GetMapping("/mine")
    public List<Reservation> getMyReservations(@AuthenticationPrincipal Jwt jwt) {
        return reservationService.findByUser(userId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reservation createReservation(@Valid @RequestBody CreateReservationRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        return reservationService.create(userId(jwt), request);
    }

    @PatchMapping("/{id}/cancel")
    public Reservation cancelReservation(@PathVariable Integer id,
                                         @AuthenticationPrincipal Jwt jwt) {
        return reservationService.cancel(id, userId(jwt), isAdmin(jwt));
    }

    private Integer userId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        return userId.intValue();
    }

    private boolean isAdmin(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaimAsString("role"));
    }
}