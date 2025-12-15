package ru.practicum.ewm.service.stats;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.util.Constants;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.EndpointHit;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsFacadeImpl implements StatsFacade {

    private final StatsClient statsClient;

    @Override
    public void hit(HttpServletRequest request) {
        try {
            EndpointHit hit = new EndpointHit();
            hit.setApp(Constants.APP_NAME);
            hit.setUri(request.getRequestURI());
            hit.setIp(extractIp(request));
            hit.setTimestamp(LocalDateTime.now());
            statsClient.hit(hit);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public Map<String, Long> getViewsByUris(List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return Collections.emptyMap();
        }
        return uris.stream().collect(java.util.stream.Collectors.toMap(u -> u, u -> 1L));
    }

    @Override
    public Map<Long, Long> getViewsByEventIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return eventIds.stream().collect(java.util.stream.Collectors.toMap(id -> id, id -> 1L));
    }

    @Override
    public long getViewsByEventId(Long eventId) {
        return eventId == null ? 0L : 1L;
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
