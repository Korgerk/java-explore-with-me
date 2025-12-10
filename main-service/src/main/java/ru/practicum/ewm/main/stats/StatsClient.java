package ru.practicum.ewm.main.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsClient {

    private final RestTemplate restTemplate;

    @Value("${stats-server.url:http://localhost:9090}")
    private String statsServerUrl;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // === Отправка события просмотра ===
    public void hit(HttpServletRequest request) {
        String app = "ewm-main-service";
        String uri = request.getRequestURI();
        String ip = getClientIp(request);

        if (ip == null || ip.isBlank()) {
            ip = "0.0.0.0";
        }

        EndpointHitDto hit = EndpointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(hit, headers);

        try {
            restTemplate.postForObject(statsServerUrl + "/hit", requestEntity, Object.class);
            log.debug("Sent hit to stats: {} from IP {}", uri, ip);
        } catch (Exception e) {
            log.warn("Failed to send hit to stats service: {}", e.getMessage());
        }
    }

    // === Получение количества просмотров по URI ===
    public Map<Long, Long> getViews(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        String start = "2000-01-01 00:00:00"; // с момента запуска сервиса
        String end = LocalDateTime.now().format(FORMATTER);

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        String url = UriComponentsBuilder.fromHttpUrl(statsServerUrl + "/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .queryParam("unique", false) // по ТЗ — общее количество, не уникальные IP
                .toUriString();

        try {
            ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(url, ViewStatsDto[].class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return mapStatsToEventViews(response.getBody(), eventIds);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch stats from stats service: {}", e.getMessage());
        }
        return eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
    }

    // === Вспомогательные методы ===

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Map<Long, Long> mapStatsToEventViews(ViewStatsDto[] stats, List<Long> eventIds) {
        Map<String, Long> uriToHits = new java.util.HashMap<>();
        for (ViewStatsDto stat : stats) {
            uriToHits.put(stat.getUri(), (long) stat.getHits());
        }

        return eventIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> uriToHits.getOrDefault("/events/" + id, 0L)
                ));
    }
}