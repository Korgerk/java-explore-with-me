package ru.practicum.mainserver.dto.compilation;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.mainserver.dto.event.EventShortDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationDto {
    Long id;
    String title;
    Boolean pinned;
    List<EventShortDto> events;
}