package ru.practicum.ewm.stats.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.model.EndpointHit;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class EndpointHitMapperTest {
    @Autowired
    private EndpointHitMapper endpointHitMapper;

    @Test
    void toEndpointHit_ShouldMapCorrect() {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("ewm-main")
                .uri("/events")
                .ip("10.10.255.0")
                .timestamp(LocalDateTime.now())
                .build();

        EndpointHit hit = endpointHitMapper.toEndpointHit(dto);

        assertNotNull(hit);
        assertEquals("ewm-main", hit.getApp());
        assertEquals("/events", hit.getUri());
    }

    @Test
    void toEndpointHitDto_ShouldMapCorrect() {
        EndpointHit hit = EndpointHit.builder()
                .id(1L)
                .app("ewm-main")
                .uri("/events")
                .ip("10.10.255.0")
                .timestamp(LocalDateTime.now())
                .build();

        EndpointHitDto dto = endpointHitMapper.toEndpointHitDto(hit);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("ewm-main", dto.getApp());
        assertEquals("/events", dto.getUri());
    }
}