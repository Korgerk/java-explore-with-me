package ru.practicum.explorewithme.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.explorewithme.model.enums.RequestStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;
    RequestStatus status;
}