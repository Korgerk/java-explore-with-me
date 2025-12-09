package ru.practicum.statsserver.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.statsdto.EndpointHit;
import ru.practicum.statsserver.model.Stats;

@Component
public class StatsMapper {

    public Stats toStats(EndpointHit endpointHit) {
        if (endpointHit == null) {
            return null;
        }

        return Stats.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp())
                .build();
    }

    public EndpointHit toEndpointHit(Stats stats) {
        if (stats == null) {
            return null;
        }

        return EndpointHit.builder()
                .id(stats.getId())
                .app(stats.getApp())
                .uri(stats.getUri())
                .ip(stats.getIp())
                .timestamp(stats.getTimestamp())
                .build();
    }
}