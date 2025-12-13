package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.dto.location.Location;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventUserRequest {
    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов")
    String annotation;

    Long category;

    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов")
    String description;

    @Future(message = "Дата события должна быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    Location location;

    Boolean paid;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    Integer participantLimit;

    Boolean requestModeration;

    String stateAction;

    @Size(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов")
    String title;
}