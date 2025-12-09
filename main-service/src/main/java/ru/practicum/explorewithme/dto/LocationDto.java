package ru.practicum.explorewithme.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationDto {
    @NotNull
    Float lat;

    @NotNull
    Float lon;
}