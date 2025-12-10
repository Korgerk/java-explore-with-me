package ru.practicum.ewm.main.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    public ResponseEntity<ApiError> handleNotFound(final NotFoundException e) {
        return buildError(e.getMessage(), "The required object was not found.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleConflict(final ConflictException e) {
        return buildError(e.getMessage(), "For the requested operation the conditions are not met.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleValidation(final ValidationException e) {
        return buildError(e.getMessage(), "Incorrectly made request.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleConstraintViolation(final ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> String.format("Field: %s. Error: %s. Value: %s",
                        v.getPropertyPath(), v.getMessage(), v.getInvalidValue()))
                .collect(Collectors.joining("; "));
        return buildError(message, "Incorrectly made request.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(final MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Field: %s. Error: %s. Value: %s",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .collect(Collectors.joining("; "));
        return buildError(message, "Incorrectly made request.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleTypeMismatch(final MethodArgumentTypeMismatchException e) {
        String message = String.format("Failed to convert value of type %s to required type %s; nested exception is java.lang.NumberFormatException: For input string: %s",
                e.getValue().getClass().getSimpleName(), e.getRequiredType(), e.getValue());
        return buildError(message, "Incorrectly made request.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleGeneric(final RuntimeException e) {
        return buildError(e.getMessage(), "Internal error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> buildError(String message, String reason, HttpStatus status) {
        ApiError error = ApiError.builder()
                .errors(Collections.emptyList())
                .message(message)
                .reason(reason)
                .status(status.name())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
        return ResponseEntity.status(status).body(error);
    }
}