package ru.practicum.ewm.dto.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults (level = AccessLevel.PRIVATE)
public class EventFullDto {

    Long id;
    String title;
    String annotation;
    String description;
    Long category;
    LocalDateTime eventDate;
    LocationDto location;
    boolean paid;
    int participantLimit;
    boolean requestModeration;
    String state;
    Long confirmedRequests;
    Long views;
    LocalDateTime createdOn;
    LocalDateTime publishedOn;
    Long initiator;
}
