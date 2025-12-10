package ru.practicum.mainserver.dto.location;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location {
    @NotNull(message = "Широта не может быть null")
    Float lat;

    @NotNull(message = "Долгота не может быть null")
    Float lon;
}