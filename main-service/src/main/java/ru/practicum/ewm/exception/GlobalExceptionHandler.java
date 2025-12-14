package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.dto.error.ApiError;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, BadRequestException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception ex) {
        return new ResponseEntity<>(new ApiError(List.of(ex.getMessage()), "Incorrectly made request.", ex.getMessage(), HttpStatus.BAD_REQUEST.name(), LocalDateTime.now()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        return new ResponseEntity<>(new ApiError(List.of(ex.getMessage()), "For the requested operation the conditions are not met.", ex.getMessage(), HttpStatus.CONFLICT.name(), LocalDateTime.now()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return new ResponseEntity<>(new ApiError(List.of(ex.getMessage()), "The required object was not found.", ex.getMessage(), HttpStatus.NOT_FOUND.name(), LocalDateTime.now()), HttpStatus.NOT_FOUND);
    }
}
