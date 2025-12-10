package ru.practicum.ewm.exception;

import javax.validation.ValidationException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewm.dto.ErrorDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestControllerAdvice
public class ExceptionErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn("Ошибка валидации параметров: {}", e.getMessage());

        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .sorted().collect(Collectors.toList());

        ErrorDto errorDto = ErrorDto.builder()
                .errors(errors)
                .message("Ошибка валидации параметров")
                .reason("Неверные данные запроса")
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Ошибка валидации параметров: {}", e.getMessage());

        ErrorDto errorDto = ErrorDto.builder()
                .message("Ошибка валидации параметров")
                .reason("Неверные данные запроса")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidation(ValidationException e) {
        log.warn("Ошибка валидации параметров: {}", e.getMessage());

        ErrorDto errorDto = ErrorDto.builder()
                .message("Ошибка валидации параметров")
                .reason(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception e) {
        log.error("НЕПРЕДВИДЕННАЯ ОШИБКА ОБРАБОТКИ: {}", e.getMessage());

        ErrorDto errorDto = ErrorDto.builder()
                .message("НЕПРЕДВИДЕННАЯ ОШИБКА")
                .reason("а пёс его знает...")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException e) {
        log.warn("Зафиксирован NOT FOUND: {}", e.getMessage());

        ErrorDto errorDto = ErrorDto.builder()
                .message(e.getMessage())
                .reason("Либо не нашли, либо неверно указан запрос")
                .status(HttpStatus.NOT_FOUND.name())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);

    }

    @ExceptionHandler(ConflictDataException.class)
    public ResponseEntity<Object> handleConflict(ConflictDataException e) {
        log.warn("Зафиксирован CONFLICT: {}", e.getMessage());

        ErrorDto errorDto = ErrorDto.builder()
                .message(e.getMessage())
                .reason("Пересекается с другими данными")
                .status(HttpStatus.CONFLICT.name())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorDto);

    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleForbidden(ForbiddenException e) {
        log.warn("Зафиксирован FORBIDDEN: {}", e.getMessage());

        ErrorDto errorDto = ErrorDto.builder()
                .message(e.getMessage())
                .reason("Запрещено трогать чужое")
                .status(HttpStatus.FORBIDDEN.name())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorDto);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        log.warn("Отсутствуют обязательные параметры запроса");

        ErrorDto errorDto = ErrorDto.builder()
                .message(e.getMessage())
                .reason("Отсутствуют обязательные параметры запроса")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }
}