package ru.practicum.ewm.main.dto;

import lombok.Data;
import ru.practicum.ewm.main.model.RequestStatus;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    @NotNull
    private RequestStatus status; // CONFIRMED или REJECTED
}