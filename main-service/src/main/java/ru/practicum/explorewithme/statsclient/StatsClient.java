package ru.practicum.explorewithme.statsclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.explorewithme.statsclient.dto.EndpointHitDto;
import ru.practicum.explorewithme.statsclient.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(RestTemplate restTemplate, @Value("${stats-server.url}") String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    public void saveHit(EndpointHitDto hitDto) {
        restTemplate.postForEntity(serverUrl + "/hit", hitDto, Void.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "end", end.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "unique", unique,
                "uris", uris == null ? "" : String.join(",", uris)
        );

        String uri = serverUrl + "/stats?start={start}&end={end}&unique={unique}" +
                     (uris != null && !uris.isEmpty() ? "&uris={uris}" : "");

        ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(uri, ViewStatsDto[].class, parameters);
        return List.of(response.getBody());
    }
}