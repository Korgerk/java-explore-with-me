package ru.practicum.ewm.main.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class NewCompilationDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

    private List<Long> events;
    private Boolean pinned = false;
}