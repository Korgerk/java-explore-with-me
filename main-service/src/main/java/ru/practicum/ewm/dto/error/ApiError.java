package ru.practicum.ewm.dto.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiError {
    List<String> errors;
    String message;
    String reason;
    HttpStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;

    public static ApiError create(HttpStatus status, String message, String reason) {
        return ApiError.builder()
                .status(status)
                .message(message)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .build();
    }
}