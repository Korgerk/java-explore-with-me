package ru.practicum.stats;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewStats {
    private String app;
    private String uri;
    private Long hits;
}