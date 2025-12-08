package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.Hit;
import ru.practicum.stats.ViewStats;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsRepository statsRepository;

    @Transactional
    public void save(Hit hit) {
        if (hit.getApp() == null || hit.getApp().isBlank()) {
            throw new IllegalArgumentException("Field 'app' is required");
        }
        if (hit.getUri() == null || hit.getUri().isBlank()) {
            throw new IllegalArgumentException("Field 'uri' is required");
        }
        if (hit.getIp() == null || hit.getIp().isBlank()) {
            throw new IllegalArgumentException("Field 'ip' is required");
        }

        EndpointHit entity = new EndpointHit();
        entity.setApp(hit.getApp());
        entity.setUri(hit.getUri());
        entity.setIp(hit.getIp());
        entity.setTimestamp(hit.getTimestamp() != null ? hit.getTimestamp() : LocalDateTime.now());
        statsRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        List<EndpointHit> hits = uris == null || uris.isEmpty()
                ? statsRepository.findHits(start, end, null)
                : statsRepository.findHits(start, end, uris);

        if (unique) {
            Set<String> seen = new HashSet<>();
            hits = hits.stream()
                    .filter(hit -> seen.add(hit.getIp() + hit.getUri()))
                    .collect(Collectors.toList());
        }

        Map<String, Long> statsMap = new LinkedHashMap<>();
        for (EndpointHit hit : hits) {
            String key = hit.getApp() + "|" + hit.getUri();
            statsMap.put(key, statsMap.getOrDefault(key, 0L) + 1);
        }

        List<ViewStats> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : statsMap.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 2);
            result.add(new ViewStats(parts[0], parts[1], entry.getValue()));
        }

        return result;
    }
}