package ru.practicum.ewm.dto.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventFullDto {

    private Long id;
    private String title;
    private String annotation;
    private String description;
    private Long category;
    private LocalDateTime eventDate;
    private LocationDto location;
    private boolean paid;
    private int participantLimit;
    private boolean requestModeration;
    private String state;
    private Long confirmedRequests;
    private Long views;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;
    private Long initiator;
}
