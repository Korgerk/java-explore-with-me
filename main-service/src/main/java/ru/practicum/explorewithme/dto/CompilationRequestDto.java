package ru.practicum.explorewithme.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationRequestDto {
    List<Long> events;

    @Builder.Default
    Boolean pinned = false;

    @NotBlank
    @Size(min = 1, max = 50)
    String title;
}