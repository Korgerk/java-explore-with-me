package ru.practicum.explorewithme.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location {
    Float lat;
    Float lon;
}