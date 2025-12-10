package ru.practicum.explorewithme.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private List<String> errors;        // всегда пустой список по ТЗ
    private String message;             // сообщение об ошибке
    private String reason;              // общее описание причины
    private String status;              // HTTP-статус (например, "NOT_FOUND")
    private LocalDateTime timestamp;    // время ошибки в формате "yyyy-MM-dd HH:mm:ss"
}