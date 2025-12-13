package ru.practicum.ewm.service.stats;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface StatsFacade {
    void hit(HttpServletRequest request);

    Map<String, Long> getViewsByUris(List<String> uris);

    Map<Long, Long> getViewsByEventIds(List<Long> eventIds);

    long getViewsByEventId(Long eventId);
}
