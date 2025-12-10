package ru.practicum.ewm.dto;

import javax.validation.constraints.NotNull;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    @NotNull
    private Float lat;

    @NotNull
    private Float lon;

}