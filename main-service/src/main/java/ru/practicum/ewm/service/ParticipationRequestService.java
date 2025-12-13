package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.participation.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.participation.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.participation.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventParticipationRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest);
}