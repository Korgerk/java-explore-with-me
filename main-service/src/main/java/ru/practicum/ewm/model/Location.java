package ru.practicum.ewm.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Location {

    @Column(name = "lat")
    private Float lat;

    @Column(name = "lon")
    private Float lon;

}