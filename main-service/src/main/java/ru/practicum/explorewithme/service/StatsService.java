package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.client.StatsClient;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsClient statsClient;

    public void recordHit(HttpServletRequest request) {
        String app = "ewm-main-service";
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        statsClient.saveHit(app, uri, ip);
        log.debug("Recorded hit: app={}, uri={}, ip={}", app, uri, ip);
    }

    public void recordEventView(Long eventId, HttpServletRequest request) {
        String app = "ewm-main-service";
        String uri = "/events/" + eventId;
        String ip = request.getRemoteAddr();

        statsClient.saveHit(app, uri, ip);
        log.debug("Recorded event view: eventId={}, ip={}", eventId, ip);
    }

    public Long getViewsForEvent(Long eventId) {
        return statsClient.getEventViews(eventId);
    }

    public Map<Long, Long> getViewsForEvents(java.util.List<Long> eventIds) {
        return statsClient.getEventsViews(eventIds);
    }
}