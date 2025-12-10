package ru.practicum.ewm.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.mapper.RequestMapper;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.model.EventState;
import ru.practicum.ewm.main.model.ParticipationRequest;
import ru.practicum.ewm.main.model.RequestStatus;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.ParticipationRequestRepository;
import ru.practicum.ewm.main.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot create request to own event");
        }

        if (requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Request already exists");
        }

        if (event.getParticipantLimit() > 0) {
            long confirmed = requestRepository.countConfirmedByEventId(eventId);
            if (confirmed >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached");
            }
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("User not found")))
                .created(LocalDateTime.now())
                .status(event.getRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build();

        ParticipationRequest saved = requestRepository.save(request);
        return RequestMapper.toDto(saved);
    }

    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Not your request");
        }
        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updated = requestRepository.save(request);
        return RequestMapper.toDto(updated);
    }

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found or not owned"));
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult updateStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest update) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        List<ParticipationRequest> requests = requestRepository.findByIds(update.getRequestIds());
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        long currentConfirmed = requestRepository.countConfirmedByEventId(eventId);
        long limit = event.getParticipantLimit();

        for (ParticipationRequest r : requests) {
            if (!r.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Request must be in PENDING status");
            }

            if (update.getStatus() == RequestStatus.CONFIRMED) {
                if (limit > 0 && currentConfirmed >= limit) {
                    throw new ConflictException("The participant limit has been reached");
                }
                r.setStatus(RequestStatus.CONFIRMED);
                currentConfirmed++;
                confirmed.add(RequestMapper.toDto(r));
            } else if (update.getStatus() == RequestStatus.REJECTED) {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toDto(r));
            }
        }

        // Если достигнут лимит — отклонить все остальные PENDING
        if (limit > 0 && currentConfirmed >= limit) {
            List<ParticipationRequest> pending = requestRepository.findByEventId(eventId).stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .collect(Collectors.toList());
            for (ParticipationRequest r : pending) {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toDto(r));
            }
        }

        requestRepository.saveAll(requests);
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }
}