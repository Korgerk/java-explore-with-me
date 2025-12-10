package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.RequestStatusUpdateDto;
import ru.practicum.ewm.dto.RequestStatusUpdateResult;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto addRequest(long userId, long eventId);

    List<ParticipationRequestDto> getUserRequests(long userId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);

    List<ParticipationRequestDto> getEventParticipants(long userId, long eventId);

    RequestStatusUpdateResult changeRequestStatus(long userId, long eventId, RequestStatusUpdateDto updateRequestDto);
}