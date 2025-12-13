package ru.practicum.ewm.service.stats;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.util.Constants;
import java.ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.EndpointHit;
import ru.practicum.statsdto.ViewStats;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsFacadeImpl implements StatsFacade {

    private final StatsClient statsClient;

    @Override
    public void hit(HttpServletRequest request) {
        EndpointHit hit = new EndpointHit();
        hit.setApp(Constants.APP_NAME);
        hit.setUri(request.getRequestURI());
        hit.setIp(request.getRemoteAddr());
        hit.setTimestamp(LocalDateTime.now());
        statsClient.hit(hit);
    }

    @Override
    public Map<String, Long> getViewsByUris(List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDateTime start = LocalDateTime.now().minusYears(10);
        LocalDateTime end = LocalDateTime.now().plusYears(1);

        List<ViewStats> stats = statsClient.getStats(start, end, uris, false);

        Map<String, Long> result = new HashMap<>();
        for (String uri : uris) {
            result.put(uri, 0L);
        }
        if (stats != null) {
            for (ViewStats dto : stats) {
                if (dto.getUri() != null) {
                    result.put(dto.getUri(), dto.getHits());
                }
            }
        }
        return result;
    }

    @Override
    public Map<Long, Long> getViewsByEventIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        Map<String, Long> byUri = getViewsByUris(uris);
        Map<Long, Long> result = new HashMap<>();
        for (Long id : eventIds) {
            result.put(id, byUri.getOrDefault("/events/" + id, 0L));
        }
        return result;
    }

    @Override
    public long getViewsByEventId(Long eventId) {
        return getViewsByEventIds(List.of(eventId)).getOrDefault(eventId, 0L);
    }
}
