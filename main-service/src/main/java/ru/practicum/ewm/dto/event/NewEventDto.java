package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewEventDto {
    @NotBlank
    private String title;

    @NotBlank
    private String annotation;

    @NotBlank
    private String description;

    @NotNull
    private Long category;

    @NotNull
    @Future
    private LocalDateTime eventDate;

    private boolean paid;
    private int participantLimit;
    private boolean requestModeration = true;

    @NotNull
    private Double lat;

    @NotNull
    private Double lon;
}
