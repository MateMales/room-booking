package com.mate.room_booking.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CreateReservationRequestTest {

    // isti "proverivač" koji Spring koristi za @Valid
    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private static final LocalDateTime FUTURE = LocalDateTime.of(2030, 1, 15, 10, 0);
    private static final LocalDateTime PAST   = LocalDateTime.of(2020, 1, 15, 10, 0);

    // vraća imena polja koja nisu prošla proveru, npr. ["startTime"]
    private Set<String> invalidFields(CreateReservationRequest request) {
        Set<ConstraintViolation<CreateReservationRequest>> violations = validator.validate(request);
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void validRequest_passes() {
        var request = new CreateReservationRequest(1, 3, FUTURE, FUTURE.plusHours(2), "Lab");

        assertThat(invalidFields(request)).isEmpty();
    }

    @Test
    void startInPast_isRejected() {
        var request = new CreateReservationRequest(1, 3, PAST, PAST.plusHours(2), "Lab");

        assertThat(invalidFields(request)).containsExactly("startTime");
    }

    @Test
    void missingUserId_isRejected() {
        var request = new CreateReservationRequest(null, 3, FUTURE, FUTURE.plusHours(2), "Lab");

        assertThat(invalidFields(request)).containsExactly("userId");
    }

    @Test
    void purposeLongerThan200_isRejected() {
        var request = new CreateReservationRequest(1, 3, FUTURE, FUTURE.plusHours(2), "x".repeat(201));

        assertThat(invalidFields(request)).containsExactly("purpose");
    }
}