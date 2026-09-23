package com.add2numweb.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        String detail = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail problem = ProblemDetails.create(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                detail,
                request);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return ProblemDetails.create(
                HttpStatus.BAD_REQUEST,
                "Malformed request",
                "Request body is missing or contains invalid JSON.",
                request);
    }

    @ExceptionHandler(WorkOrderIdGenerationException.class)
    public ProblemDetail handleIdGeneration(
            WorkOrderIdGenerationException exception,
            HttpServletRequest request) {
        return ProblemDetails.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Work order ID generation failed",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccess(
            DataAccessException exception,
            HttpServletRequest request) {
        return ProblemDetails.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Persistence failure",
                "The work order could not be persisted.",
                request);
    }
}
