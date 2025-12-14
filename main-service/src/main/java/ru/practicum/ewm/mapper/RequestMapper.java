package ru.practicum.ewm.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.model.ParticipationRequest;

@Component
public class RequestMapper {

    public ParticipationRequestDto toDto(ParticipationRequest request) {
        if (request == null) {
            return null;
        }
        return new ParticipationRequestDto(
                request.getId(),
                request.getEvent() == null ? null : request.getEvent().getId(),
                request.getRequester() == null ? null : request.getRequester().getId(),
                request.getStatus(),
                request.getCreated()
        );
    }
}
