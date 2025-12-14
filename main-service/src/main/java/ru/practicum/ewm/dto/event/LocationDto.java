package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationDto {

    @NotNull
    private Double lat;

    @NotNull
    private Double lon;
}
