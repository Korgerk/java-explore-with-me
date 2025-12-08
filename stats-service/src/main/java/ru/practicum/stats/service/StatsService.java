package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.Hit;
import ru.practicum.stats.ViewStats;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
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

        List<EndpointHit> hits;
        if (uris == null || uris.isEmpty()) {
            hits = statsRepository.findHits(start, end, null);
        } else {
            hits = statsRepository.findHits(start, end, uris);
        }

        if (unique) {
            hits = hits.stream().collect(Collectors.toMap(hit -> hit.getIp() + hit.getUri(), hit -> hit, (hit1, hit2) -> hit1, LinkedHashMap::new)).values().stream().toList();
        }

        return hits.stream().collect(Collectors.groupingBy(hit -> hit.getApp() + "|" + hit.getUri(), Collectors.counting())).entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("\\|", 2);
            return new ViewStats(parts[0], parts[1], entry.getValue());
        }).collect(Collectors.toList());
    }
}