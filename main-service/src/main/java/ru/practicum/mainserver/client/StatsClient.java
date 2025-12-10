package ru.practicum.mainserver.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.statsdto.EndpointHit;
import ru.practicum.statsdto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class StatsClient {
    private final RestTemplate rest;
    private final String serverUrl;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-service.url}") String serverUrl) {
        this.rest = new RestTemplate();
        this.serverUrl = serverUrl;
        log.info("StatsClient initialized with server URL: {}", serverUrl);
    }

    public void hit(EndpointHit endpointHit) {
        log.debug("Sending hit to stats service: {}", endpointHit);
        makeAndSendRequest("/hit", HttpMethod.POST, null, endpointHit);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    @Nullable List<String> uris, boolean unique) {
        log.debug("Getting stats from stats service: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", encodeDateTime(start));
        parameters.put("end", encodeDateTime(end));

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", "{start}")
                .queryParam("end", "{end}");

        if (uris != null && !uris.isEmpty()) {
            parameters.put("uris", String.join(",", uris));
            builder.queryParam("uris", "{uris}");
        }

        if (unique) {
            parameters.put("unique", true);
            builder.queryParam("unique", "{unique}");
        }

        String path = builder.encode().toUriString();

        ResponseEntity<ViewStats[]> response = makeAndSendRequest(
                path, HttpMethod.GET, parameters, null, ViewStats[].class);

        if (response != null && response.getBody() != null) {
            return Arrays.asList(response.getBody());
        }
        return Collections.emptyList();
    }

    public Long getEventViews(Long eventId, LocalDateTime start, LocalDateTime end) {
        log.debug("Getting views for event id={}, start={}, end={}", eventId, start, end);

        String eventUri = "/events/" + eventId;
        List<String> uris = Collections.singletonList(eventUri);

        List<ViewStats> stats = getStats(start, end, uris, false);

        if (stats.isEmpty()) {
            return 0L;
        }

        return stats.get(0).getHits();
    }

    public Map<Long, Long> getEventsViews(List<Long> eventIds, LocalDateTime start, LocalDateTime end) {
        log.debug("Getting views for multiple events: ids={}, start={}, end={}", eventIds, start, end);

        Map<Long, Long> viewsMap = new HashMap<>();

        if (eventIds == null || eventIds.isEmpty()) {
            return viewsMap;
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        List<ViewStats> stats = getStats(start, end, uris, false);

        for (ViewStats stat : stats) {
            try {
                String uri = stat.getUri();
                Long eventId = Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
                viewsMap.put(eventId, stat.getHits());
            } catch (NumberFormatException e) {
                log.warn("Failed to parse event ID from URI: {}", stat.getUri());
            }
        }

        return viewsMap;
    }

    private String encodeDateTime(LocalDateTime dateTime) {
        return URLEncoder.encode(dateTime.format(FORMATTER), StandardCharsets.UTF_8);
    }

    private <T> ResponseEntity<T> makeAndSendRequest(String path, HttpMethod method,
                                                     @Nullable Map<String, Object> parameters,
                                                     @Nullable Object body,
                                                     Class<T> responseType) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<T> response;
        try {
            if (parameters != null) {
                response = rest.exchange(serverUrl + path, method, requestEntity, responseType, parameters);
            } else {
                response = rest.exchange(serverUrl + path, method, requestEntity, responseType);
            }
            log.debug("Stats service response status: {}", response.getStatusCode());
        } catch (HttpStatusCodeException e) {
            log.error("Error from stats service: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error communicating with stats service: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error communicating with stats service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to communicate with stats service", e);
        }

        return response;
    }

    private void makeAndSendRequest(String path, HttpMethod method,
                                    @Nullable Map<String, Object> parameters,
                                    @Nullable Object body) {
        makeAndSendRequest(path, method, parameters, body, Void.class);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}