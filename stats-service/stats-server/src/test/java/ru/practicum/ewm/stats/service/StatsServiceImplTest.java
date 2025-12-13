package ru.practicum.ewm.stats.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.mapper.EndpointHitMapper;
import ru.practicum.ewm.stats.model.EndpointHit;
import ru.practicum.ewm.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatsServiceImplTest {

    @Mock
    private StatsRepository statsRepository;

    @Mock
    private EndpointHitMapper endpointHitMapper;

    @InjectMocks
    private StatsServiceImpl statsService;

    @Test
    void addHit_SuccessCase_ShouldSave() {
        var ldt = LocalDateTime.now();

        EndpointHitDto dto = EndpointHitDto.builder()
                .app("ewm-main")
                .uri("/events")
                .ip("10.10.255.0")
                .timestamp(ldt)
                .build();

        EndpointHit hit = EndpointHit.builder()
                .id(1L)
                .app("ewm-main")
                .uri("/events")
                .ip("10.10.255.0")
                .timestamp(ldt)
                .build();

        when(endpointHitMapper.toEndpointHit(dto))
                .thenReturn(EndpointHit.builder()
                        .app(dto.getApp())
                        .uri(dto.getUri())
                        .ip(dto.getIp())
                        .timestamp(dto.getTimestamp())
                        .build()
                );

        when(endpointHitMapper.toEndpointHitDto(hit))
                .thenReturn(EndpointHitDto.builder()
                        .id(hit.getId())
                        .app(hit.getApp())
                        .uri(hit.getUri())
                        .ip(hit.getIp())
                        .timestamp(hit.getTimestamp())
                        .build()
                );

        when(statsRepository.save(any(EndpointHit.class)))
                .thenReturn(hit);

        var result = statsService.addHit(dto);

        assertEquals(hit.getId(), result.getId());
        assertEquals(hit.getApp(), result.getApp());
        assertEquals(hit.getIp(), result.getIp());
        assertEquals(hit.getTimestamp(), result.getTimestamp());
        verify(statsRepository, times(1)).save(any());
    }

    @Test
    void getStats_UniqueWithoutUris_ShouldUseCorrectMethod() {

        var end = LocalDateTime.now();
        var start = end.minusDays(1);
        List<ViewStatsDto> statsUnique = List.of(new ViewStatsDto("evm-main", "/events", 2L));

        when(statsRepository.getUniqueStats(start, end))
                .thenReturn(statsUnique);

        var result = statsService.getStats(start, end, null, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(statsRepository, times(1)).getUniqueStats(any(), any());
        verify(statsRepository, never()).getNotUniqueStats(any(), any());
    }

    @Test
    void getStats_NotUniqueWithoutUris_ShouldUseCorrectMethod() {

        var end = LocalDateTime.now();
        var start = end.minusDays(1);
        List<ViewStatsDto> statsNotUnique = List.of(new ViewStatsDto("evm-main", "/events", 5L));

        when(statsRepository.getNotUniqueStats(start, end))
                .thenReturn(statsNotUnique);

        var result = statsService.getStats(start, end, null, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(statsRepository, times(1)).getNotUniqueStats(any(), any());
        verify(statsRepository, never()).getUniqueStats(any(), any());

    }

    @Test
    void getStats_UniqueWithUris_ShouldUseCorrectMethod() {

        var end = LocalDateTime.now();
        var start = end.minusDays(1);
        List<String> uris = List.of("/events");
        List<ViewStatsDto> statsUnique = List.of(new ViewStatsDto("evm-main", "/events", 2L));

        when(statsRepository.getUniqueStatsForUris(start, end, uris))
                .thenReturn(statsUnique);

        var result = statsService.getStats(start, end, uris, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(statsRepository, times(1)).getUniqueStatsForUris(any(), any(), any());
        verify(statsRepository, never()).getNotUniqueStatsForUris(any(), any(), any());
    }
}
