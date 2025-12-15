package ru.practicum.ewm.dto.rating;

import jakarta.validation.constraints.NotNull;
import ru.practicum.ewm.model.enums.RatingValue;

public class RatingRequestDto {

    @NotNull
    private RatingValue value;

    public RatingRequestDto() {
    }

    public RatingRequestDto(RatingValue value) {
        this.value = value;
    }

    public RatingValue getValue() {
        return value;
    }

    public void setValue(RatingValue value) {
        this.value = value;
    }
}
