package ru.practicum.ewm.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewCategoryDto {
    @NotBlank
    private String name;
}
