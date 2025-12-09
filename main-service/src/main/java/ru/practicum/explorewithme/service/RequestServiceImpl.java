package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.ForbiddenException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.RequestMapper;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.Request;
import ru.practicum.explorewithme.model.User;
import ru.practicum.explorewithme.model.enums.EventState;
import ru.practicum.explorewithme.model.enums.RequestStatus;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        List<Request> requests = requestRepository.findAllByRequesterId(userId);

        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        // Проверка, что пользователь не является инициатором события
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Event initiator cannot create request for his own event");
        }

        // Проверка, что событие опубликовано
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        // Проверка на повторный запрос
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists for this event");
        }

        // Проверка лимита участников
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() != 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Event participant limit reached");
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .build();

        // Если премодерация отключена, автоматически подтверждаем
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        Request savedRequest = requestRepository.save(request);
        log.info("Created request with id={} for event id={}", savedRequest.getId(), eventId);

        return requestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ForbiddenException("User is not the owner of this request");
        }

        request.setStatus(RequestStatus.CANCELED);
        Request updatedRequest = requestRepository.save(request);
        log.info("Canceled request with id={}", requestId);

        return requestMapper.toParticipationRequestDto(updatedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("User is not the initiator of this event");
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);

        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("User is not the initiator of this event");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Event does not require request moderation");
        }

        List<Request> requests = requestRepository.findByIdIn(updateRequest.getRequestIds());

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long availableSlots = event.getParticipantLimit() - confirmedRequests;

        if (availableSlots <= 0 && updateRequest.getStatus() == RequestStatus.CONFIRMED) {
            throw new ConflictException("Event participant limit reached");
        }

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (Request request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Request must be in PENDING status");
            }

            if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
                if (availableSlots > 0) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(requestMapper.toParticipationRequestDto(request));
                    availableSlots--;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejected.add(requestMapper.toParticipationRequestDto(request));
                }
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(requestMapper.toParticipationRequestDto(request));
            }
        }

        requestRepository.saveAll(requests);
        log.info("Updated {} requests for event id={}", requests.size(), eventId);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }
}