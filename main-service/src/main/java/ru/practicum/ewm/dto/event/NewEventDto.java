package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults (level = AccessLevel.PRIVATE)
public class NewEventDto {

    @NotBlank
    String annotation;

    @NotNull
    Long category;

    @NotBlank
    String description;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @NotNull
    @Valid
    LocationDto location;

    boolean paid = false;

    @Min(0)
    int participantLimit = 0;

    boolean requestModeration = true;

    @NotBlank
    String title;

}
