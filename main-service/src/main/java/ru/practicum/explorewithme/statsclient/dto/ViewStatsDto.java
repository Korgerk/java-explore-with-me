package ru.practicum.explorewithme.statsclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsDto {
    private String uri;
    private long hits;
}