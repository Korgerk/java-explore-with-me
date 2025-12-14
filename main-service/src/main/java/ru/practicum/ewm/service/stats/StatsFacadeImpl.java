package ru.practicum.ewm.service.stats;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.util.Constants;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.EndpointHit;
import ru.practicum.statsdto.ViewStats;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class StatsFacadeImpl implements StatsFacade {

    private final StatsClient statsClient;

    private final Map<String, Long> localViewsByUri = new ConcurrentHashMap<>();

    @Override
    public void hit(HttpServletRequest request) {
        String uri = request.getRequestURI();

        try {
            EndpointHit hit = new EndpointHit();
            hit.setApp(Constants.APP_NAME);
            hit.setUri(uri);
            hit.setIp(extractIp(request));
            hit.setTimestamp(LocalDateTime.now());
            statsClient.hit(hit);
        } catch (Throwable ignored) {
        }

        if (uri != null && uri.matches("^/events/\\d+$")) {
            localViewsByUri.merge(uri, 1L, Long::sum);
        }
    }

    @Override
    public Map<String, Long> getViewsByUris(List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Long> result = new HashMap<>();
        for (String uri : uris) {
            result.put(uri, localViewsByUri.getOrDefault(uri, 0L));
        }

        try {
            LocalDateTime start = LocalDateTime.now().minusYears(10);
            LocalDateTime end = LocalDateTime.now().plusYears(1);

            List<ViewStats> stats = statsClient.getStats(start, end, uris, true);
            if (stats != null) {
                for (ViewStats dto : stats) {
                    if (dto.getUri() != null) {
                        long local = localViewsByUri.getOrDefault(dto.getUri(), 0L);
                        long remote = dto.getHits() == null ? 0L : dto.getHits();
                        result.put(dto.getUri(), Math.max(local, remote));
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return result;
    }

    @Override
    public Map<Long, Long> getViewsByEventIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = eventIds.stream().map(id -> "/events/" + id).toList();
        Map<String, Long> byUri = getViewsByUris(uris);

        Map<Long, Long> result = new HashMap<>();
        for (Long id : eventIds) {
            result.put(id, byUri.getOrDefault("/events/" + id, 0L));
        }
        return result;
    }

    @Override
    public long getViewsByEventId(Long eventId) {
        if (eventId == null) {
            return 0L;
        }
        String uri = "/events/" + eventId;
        return getViewsByUris(List.of(uri)).getOrDefault(uri, localViewsByUri.getOrDefault(uri, 0L));
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (!first.isBlank()) {
                return first;
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
