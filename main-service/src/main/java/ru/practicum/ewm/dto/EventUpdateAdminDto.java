package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import lombok.*;
import ru.practicum.ewm.model.EventStateAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EventUpdateAdminDto {

    @Size(min = 3, max = 120)
    private String title;

    @Size(min = 20, max = 2000)
    private String annotation;

    @Size(min = 20, max = 7000)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @PositiveOrZero
    private Integer participantLimit;

    private Integer category;
    private LocationDto location;
    private Boolean paid;
    private Boolean requestModeration;
    private EventStateAction stateAction;
}