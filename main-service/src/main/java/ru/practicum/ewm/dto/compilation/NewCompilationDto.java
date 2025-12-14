package ru.practicum.ewm.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewCompilationDto {

    @NotBlank
    @Size(max = 50)
    String title;

    Boolean pinned;

    List<Long> events;
}
