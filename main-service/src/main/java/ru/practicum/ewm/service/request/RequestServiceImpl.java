package ru.practicum.ewm.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.model.enums.RequestStatus;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        ensureUser(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User requester = ensureUser(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event is not published");
        }
        if (requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Request already exists");
        }

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int limit = event.getParticipantLimit();

        if (limit > 0 && confirmed >= limit) {
            throw new ConflictException("Participant limit reached");
        }

        ParticipationRequest pr = new ParticipationRequest();
        pr.setEvent(event);
        pr.setRequester(requester);
        pr.setCreated(LocalDateTime.now());

        if (!event.isRequestModeration() || limit == 0) {
            pr.setStatus(RequestStatus.CONFIRMED);
        } else {
            pr.setStatus(RequestStatus.PENDING);
        }

        ParticipationRequest saved = requestRepository.save(pr);
        return requestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only initiator can view requests for this event");
        }

        return requestRepository.findByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only initiator can update requests for this event");
        }

        RequestStatus targetStatus = parseTargetStatus(request.getStatus());
        if (targetStatus != RequestStatus.CONFIRMED && targetStatus != RequestStatus.REJECTED) {
            throw new ConflictException("Unsupported status: " + request.getStatus());
        }

        List<Long> ids = request.getRequestIds() == null ? List.of() : request.getRequestIds();
        if (ids.isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(ids);
        Map<Long, ParticipationRequest> byId = requests.stream()
                .collect(Collectors.toMap(ParticipationRequest::getId, r -> r, (a, b) -> a));

        for (Long id : ids) {
            if (!byId.containsKey(id)) {
                throw new NotFoundException("Request not found: " + id);
            }
        }

        for (ParticipationRequest pr : requests) {
            if (!pr.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Request " + pr.getId() + " does not belong to event " + eventId);
            }
            if (pr.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Only PENDING requests can be updated");
            }
        }

        int limit = event.getParticipantLimit();
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        List<ParticipationRequest> confirmedList = new ArrayList<>();
        List<ParticipationRequest> rejectedList = new ArrayList<>();

        if (targetStatus == RequestStatus.REJECTED) {
            for (ParticipationRequest pr : requests) {
                pr.setStatus(RequestStatus.REJECTED);
                rejectedList.add(pr);
            }
            requestRepository.saveAll(requests);
            return new EventRequestStatusUpdateResult(
                    confirmedList.stream().map(requestMapper::toDto).toList(),
                    rejectedList.stream().map(requestMapper::toDto).toList()
            );
        }

        if (limit > 0 && confirmed >= limit) {
            throw new ConflictException("Participant limit reached");
        }

        for (ParticipationRequest pr : requests) {
            if (limit > 0 && confirmed >= limit) {
                pr.setStatus(RequestStatus.REJECTED);
                rejectedList.add(pr);
            } else {
                pr.setStatus(RequestStatus.CONFIRMED);
                confirmedList.add(pr);
                confirmed++;
            }
        }

        requestRepository.saveAll(requests);

        if (limit > 0 && confirmed >= limit) {
            List<ParticipationRequest> pending = requestRepository.findByEventId(eventId).stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .collect(Collectors.toList());
            if (!pending.isEmpty()) {
                for (ParticipationRequest pr : pending) {
                    pr.setStatus(RequestStatus.REJECTED);
                }
                requestRepository.saveAll(pending);
                rejectedList.addAll(pending);
            }
        }

        return new EventRequestStatusUpdateResult(
                confirmedList.stream().map(requestMapper::toDto).toList(),
                rejectedList.stream().map(requestMapper::toDto).toList()
        );
    }

    private User ensureUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private RequestStatus parseTargetStatus(String status) {
        if (status == null) {
            throw new ConflictException("Status is required");
        }
        try {
            return RequestStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Unknown status: " + status);
        }
    }
}
