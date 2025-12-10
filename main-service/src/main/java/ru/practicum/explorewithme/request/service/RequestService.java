package ru.practicum.explorewithme.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.request.mapper.RequestMapper;
import ru.practicum.explorewithme.request.model.ParticipationRequest;
import ru.practicum.explorewithme.request.model.RequestStatus;
import ru.practicum.explorewithme.request.repository.RequestRepository;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Cannot participate in unpublished event");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot create request to own event");
        }
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Request from user " + userId + " to event " + eventId + " already exists");
        }
        if (event.getParticipantLimit() > 0) {
            long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmed >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached");
            }
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setRequester(requester);
        request.setEvent(event);
        request.setCreated(LocalDateTime.now());

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        ParticipationRequest saved = requestRepository.save(request);
        return RequestMapper.toDto(saved);
    }

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " was not found");
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updated = requestRepository.save(request);
        return RequestMapper.toDto(updated);
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId,
                                                               EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(updateRequest.getRequestIds());
        if (requests.size() != updateRequest.getRequestIds().size()) {
            throw new NotFoundException("Some requests not found");
        }

        for (ParticipationRequest r : requests) {
            if (!r.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        List<ParticipationRequest> confirmed = List.of();
        List<ParticipationRequest> rejected = List.of();

        if ("CONFIRMED".equals(updateRequest.getStatus())) {
            if (event.getParticipantLimit() > 0) {
                long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
                int available = event.getParticipantLimit() - (int) confirmedCount;

                if (available <= 0) {
                    throw new ConflictException("The participant limit has been reached");
                }

                confirmed = requests.stream()
                        .limit(available)
                        .peek(r -> r.setStatus(RequestStatus.CONFIRMED))
                        .collect(Collectors.toList());

                rejected = requests.stream()
                        .skip(available)
                        .peek(r -> r.setStatus(RequestStatus.REJECTED))
                        .collect(Collectors.toList());
            } else {
                confirmed = requests.stream()
                        .peek(r -> r.setStatus(RequestStatus.CONFIRMED))
                        .collect(Collectors.toList());
            }
        } else if ("REJECTED".equals(updateRequest.getStatus())) {
            rejected = requests.stream()
                    .peek(r -> r.setStatus(RequestStatus.REJECTED))
                    .collect(Collectors.toList());
        }

        requestRepository.saveAll(confirmed);
        requestRepository.saveAll(rejected);

        return new EventRequestStatusUpdateResult(
                confirmed.stream().map(RequestMapper::toDto).collect(Collectors.toList()),
                rejected.stream().map(RequestMapper::toDto).collect(Collectors.toList())
        );
    }

    public long getConfirmedRequestsCount(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }
}