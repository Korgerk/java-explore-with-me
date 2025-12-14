package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {

    @NotBlank
    @Size(min = 3, max = 120)
    String title;

    @NotBlank
    @Size(min = 20, max = 2000)
    String annotation;

    @NotBlank
    @Size(min = 20, max = 7000)
    String description;

    @NotNull
    Long category;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    Boolean paid;
    Integer participantLimit;
    Boolean requestModeration;

    @NotNull
    @Valid
    LocationDto location;
}