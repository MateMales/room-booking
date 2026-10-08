package com.mate.room_booking.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex){
        String message  = ex.getBindingResult().getFieldErrors().stream()
                .map(error-> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex){
        String sqlState = findSqlState(ex);

        if("23P01".equals(sqlState)){
            return ProblemDetail.forStatusAndDetail(
                    HttpStatus.CONFLICT,
                    "Room is already booked in that time slot. ");
        }
        if ("23514".equals(sqlState)) {
            return ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "End time must be after start time, and a reservation can last at most 4 hours");
        }
        if ("23503".equals(sqlState)) {
            return ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST, "User or room does not exist");
        }
        throw ex;

    }
    private String findSqlState(Throwable ex){
        Throwable current = ex;
        while (current!=null){
            if(current instanceof SQLException sqlException) return sqlException.getSQLState();
            current=current.getCause();
        }
        return null;
    }
}
