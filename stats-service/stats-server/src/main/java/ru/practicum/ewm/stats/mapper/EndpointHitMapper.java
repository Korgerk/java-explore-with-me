package ru.practicum.ewm.stats.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.model.EndpointHit;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {

    EndpointHit toEndpointHit(EndpointHitDto endpointHitDto);

    EndpointHitDto toEndpointHitDto(EndpointHit endpointHit);
}
