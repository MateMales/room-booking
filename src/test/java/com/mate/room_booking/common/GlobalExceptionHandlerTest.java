package com.mate.room_booking.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // pravi grešku kakvu bi Spring napravio od greške iz baze
    private DataIntegrityViolationException dbError(String sqlState) {
        SQLException fromDatabase = new SQLException("database error", sqlState);
        return new DataIntegrityViolationException("data integrity violation", fromDatabase);
    }

    @Test
    void exclusionViolation_returns409() {
        ProblemDetail result = handler.handleDataIntegrity(dbError("23P01"));

        assertThat(result.getStatus()).isEqualTo(409);
    }

    @Test
    void checkViolation_returns400() {
        ProblemDetail result = handler.handleDataIntegrity(dbError("23514"));

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).contains("at most 4 hours");
    }

    @Test
    void foreignKeyViolation_returns400() {
        ProblemDetail result = handler.handleDataIntegrity(dbError("23503"));

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).contains("does not exist");
    }

    @Test
    void unknownSqlState_isRethrown() {
        DataIntegrityViolationException unknown = dbError("99999");

        assertThatThrownBy(() -> handler.handleDataIntegrity(unknown))
                .isSameAs(unknown);
    }
}