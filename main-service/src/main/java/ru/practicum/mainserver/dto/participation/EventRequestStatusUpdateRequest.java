package ru.practicum.mainserver.dto.participation;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateRequest {
    @NotNull(message = "Список идентификаторов запросов не может быть null")
    List<Long> requestIds;

    @NotNull(message = "Статус не может быть null")
    String status; // CONFIRMED или REJECTED
}