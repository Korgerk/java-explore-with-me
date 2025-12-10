package ru.practicum.explorewithme.statsclient.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsDto {
    private String uri;
    private long hits;
}