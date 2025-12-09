package ru.practicum.explorewithme.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.explorewithme.model.enums.EventState;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto {
    Long id;
    String title;
    String annotation;
    String description;
    CategoryDto category;
    UserDto initiator;
    LocationDto location;
    Boolean paid;
    Integer participantLimit;
    Boolean requestModeration;
    EventState state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdOn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime publishedOn;

    Long confirmedRequests;
    Long views;
}