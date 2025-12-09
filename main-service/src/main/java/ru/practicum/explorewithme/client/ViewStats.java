package ru.practicum.explorewithme.client;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ViewStats {
    String app;
    String uri;
    Long hits;
}