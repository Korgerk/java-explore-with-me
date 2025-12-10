package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.model.ParticipationRequest;

public class RequestMapper {

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus()
        );
    }
}