package ru.practicum.ewm.stats.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatsControllerTest {
    @Mock
    private StatsService statsService;

    @InjectMocks
    private StatsController statsController;

    @Test
    void addHit_SuccessCase_ShouldReturnSavedHit() {
        var ldt = LocalDateTime.now();

        EndpointHitDto dto = EndpointHitDto.builder()
                .app("ewm-main")
                .uri("/events")
                .ip("10.10.255.0")
                .timestamp(ldt)
                .build();

        EndpointHitDto hit = EndpointHitDto.builder()
                .id(1L)
                .app("ewm-main")
                .uri("/events")
                .ip("10.10.255.0")
                .timestamp(ldt)
                .build();


        when(statsService.addHit(any(EndpointHitDto.class)))
                .thenReturn(hit);

        var result = statsController.addHit(dto);

        assertNotNull(result);
        assertEquals(hit.getId(), result.getId());
        verify(statsService, times(1)).addHit(dto);
    }

    @Test
    void getStats_UniqueWithoutUris_ShouldReturnStats() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<ViewStatsDto> stats = List.of(new ViewStatsDto("evm-main", "/events", 2L));

        when(statsService.getStats(start, end, null, true))
                .thenReturn(stats);

        var result = statsController.getStats(start.toString(), end.toString(), null, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(statsService, times(1)).getStats(any(), any(), any(), any(boolean.class));
    }
}
