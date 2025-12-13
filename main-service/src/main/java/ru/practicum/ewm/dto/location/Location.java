package ru.practicum.ewm.dto.location;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

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