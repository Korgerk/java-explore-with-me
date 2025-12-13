package ru.practicum.ewm.stats.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.mapper.EndpointHitMapper;
import ru.practicum.ewm.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final EndpointHitMapper endpointHitMapper;

    @Override
    @Transactional
    public EndpointHitDto addHit(EndpointHitDto endpointHitDto) {
        return endpointHitMapper.toEndpointHitDto(
                statsRepository.save(endpointHitMapper.toEndpointHit(endpointHitDto))
        );
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (uris == null || uris.isEmpty()) {
            return unique ? statsRepository.getUniqueStats(start, end) : statsRepository.getNotUniqueStats(start, end);
        }

        if (start.isAfter(end)) {
            throw new ValidationException("Старт не может быть позже энд");
        }

        return unique ? statsRepository.getUniqueStatsForUris(start, end, uris)
                : statsRepository.getNotUniqueStatsForUris(start, end, uris);
    }
}
