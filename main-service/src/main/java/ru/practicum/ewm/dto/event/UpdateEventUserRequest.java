package ru.practicum.ewm.dto.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateEventUserRequest {
    private String title;
    private String annotation;
    private String description;
    private Long category;
    private LocalDateTime eventDate;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String stateAction;
}
