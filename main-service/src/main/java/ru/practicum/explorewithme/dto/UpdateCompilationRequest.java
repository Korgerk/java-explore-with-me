package ru.practicum.explorewithme.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCompilationRequest {
    List<Long> events;
    Boolean pinned;

    @Size(min = 1, max = 50)
    String title;
}