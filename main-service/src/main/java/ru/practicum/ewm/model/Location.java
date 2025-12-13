package ru.practicum.ewm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Location {

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lon")
    private Double lon;
}
