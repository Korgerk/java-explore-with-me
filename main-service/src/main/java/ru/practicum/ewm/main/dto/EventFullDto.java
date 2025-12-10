package ru.practicum.ewm.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.model.EventState;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private CategoryDto category;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;
    private LocalDateTime eventDate;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;
    private Integer participantLimit = 0;
    private Boolean requestModeration = true;
    private EventState state; // PENDING, PUBLISHED, CANCELED
    private Long views = 0L;
    private Long confirmedRequests = 0L;
}