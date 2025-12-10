package ru.practicum.ewm.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @NotNull
    private LocalDateTime eventDate; // ← не String!

    @NotNull
    private Location location;

    @NotNull
    private Long category;

    private Boolean paid = false;

    private Integer participantLimit = 0;

    private Boolean requestModeration = true;
}