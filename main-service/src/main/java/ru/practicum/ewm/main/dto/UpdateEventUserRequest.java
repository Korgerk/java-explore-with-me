package ru.practicum.ewm.main.dto;

import lombok.Data;
import ru.practicum.ewm.main.model.StateAction;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class UpdateEventUserRequest {

    @Size(min = 3, max = 120)
    private String title;

    @Size(min = 20, max = 2000)
    private String annotation;

    @Size(min = 20, max = 7000)
    private String description;

    private LocalDateTime eventDate;

    private Location location;

    private Long category;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction; // SEND_TO_REVIEW или CANCEL_REVIEW
}