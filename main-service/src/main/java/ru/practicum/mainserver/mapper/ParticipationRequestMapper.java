package ru.practicum.mainserver.mapper;

import org.mapstruct.*;
import ru.practicum.mainserver.dto.participation.ParticipationRequestDto;
import ru.practicum.mainserver.model.ParticipationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    ParticipationRequestDto toDto(ParticipationRequest request);

    List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> requests);
}