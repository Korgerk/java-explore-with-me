package ru.practicum.explorewithme.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-server.url:http://localhost:9090}") String serverUrl,
                       RestTemplateBuilder builder) {
        this.serverUrl = serverUrl;
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public void saveHit(EndpointHitDto hitDto) {
        try {
            restTemplate.postForEntity("/hit", hitDto, Void.class);
            log.debug("Hit saved successfully: app={}, uri={}, ip={}",
                    hitDto.getApp(), hitDto.getUri(), hitDto.getIp());
        } catch (Exception e) {
            log.error("Failed to save hit to stats service: {}", e.getMessage());
        }
    }

    public void saveHit(String app, String uri, String ip) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();
        saveHit(hitDto);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, Boolean unique) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serverUrl + "/stats")
                    .queryParam("start", start.format(formatter))
                    .queryParam("end", end.format(formatter))
                    .queryParam("unique", unique);

            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", String.join(",", uris));
            }

            ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(
                    builder.toUriString(), ViewStats[].class);

            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to get stats from stats service: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    public Long getEventViews(Long eventId) {
        LocalDateTime start = LocalDateTime.now().minusYears(100);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        List<String> uris = List.of("/events/" + eventId);

        List<ViewStats> stats = getStats(start, end, uris, false);

        return stats.stream()
                .filter(stat -> stat.getUri().equals("/events/" + eventId))
                .mapToLong(ViewStats::getHits)
                .sum();
    }

    public Map<Long, Long> getEventsViews(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDateTime start = LocalDateTime.now().minusYears(100);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        List<ViewStats> stats = getStats(start, end, uris, false);

        return stats.stream()
                .collect(Collectors.toMap(
                        stat -> extractEventIdFromUri(stat.getUri()),
                        ViewStats::getHits,
                        Long::sum
                ));
    }

    private Long extractEventIdFromUri(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            log.error("Failed to extract event id from uri: {}", uri);
            return -1L;
        }
    }
}