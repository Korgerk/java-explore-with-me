package ru.practicum.ewm.stats.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;


public class StatsClient {
    private final RestClient restClient;

    public StatsClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void addHit(EndpointHitDto endpointHitDto) {
        restClient.post().uri("/hit")
                .body(endpointHitDto)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Даты начала и окончания должны быть заданы");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Даты окончания должна быть позже даты начала");
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromPath("/stats")
                .queryParam("start", start)
                .queryParam("end", end);

        if (uris != null && !uris.isEmpty()) {
            uriComponentsBuilder.queryParam("uris", uris);
        }

        if (unique != null) {
            uriComponentsBuilder.queryParam("unique", unique);
        }

        return restClient.get()
                .uri(uriComponentsBuilder.encode().toUriString())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {
                });
    }
}
