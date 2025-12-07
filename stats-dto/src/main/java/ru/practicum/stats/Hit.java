package ru.practicum.stats;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hit {
    private String app;
    private String uri;
    private String ip;
    private LocalDateTime timestamp;
}