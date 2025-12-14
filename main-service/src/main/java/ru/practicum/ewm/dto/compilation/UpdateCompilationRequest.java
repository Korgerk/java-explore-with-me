package ru.practicum.ewm.dto.compilation;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data@FieldDefaults(level = AccessLevel.PRIVATE)

public class UpdateCompilationRequest {
    String title;
    Boolean pinned;
    List<Long> events;
}
