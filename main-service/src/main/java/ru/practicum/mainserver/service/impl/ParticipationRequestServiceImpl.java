package ru.practicum.mainserver.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainserver.dto.event.EventState;
import ru.practicum.mainserver.dto.participation.EventRequestStatusUpdateRequest;
import ru.practicum.mainserver.dto.participation.EventRequestStatusUpdateResult;
import ru.practicum.mainserver.dto.participation.ParticipationRequestDto;
import ru.practicum.mainserver.dto.participation.ParticipationStatus;
import ru.practicum.mainserver.exception.ConflictException;
import ru.practicum.mainserver.exception.NotFoundException;
import ru.practicum.mainserver.exception.ValidationException;
import ru.practicum.mainserver.mapper.ParticipationRequestMapper;
import ru.practicum.mainserver.model.Event;
import ru.practicum.mainserver.model.ParticipationRequest;
import ru.practicum.mainserver.model.User;
import ru.practicum.mainserver.repository.EventRepository;
import ru.practicum.mainserver.repository.ParticipationRequestRepository;
import ru.practicum.mainserver.repository.UserRepository;
import ru.practicum.mainserver.service.ParticipationRequestService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание запроса на участие пользователем id={} в событии id={}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать заявку на участие в своём событии");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Запрос от пользователя id=" + userId + " на участие в событии id=" + eventId + " уже существует");
        }

        if (event.getParticipantLimit() > 0 &&
            event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников для события id=" + eventId);
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .status(event.getRequestModeration() ? ParticipationStatus.PENDING : ParticipationStatus.CONFIRMED)
                .build();

        if (!event.getRequestModeration()) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        log.info("Запрос на участие создан с id: {}", savedRequest.getId());

        return requestMapper.toDto(savedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя id={}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        return requestMapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса id={} пользователем id={}", requestId, userId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("Запрос не принадлежит пользователю id=" + userId);
        }

        request.setStatus(ParticipationStatus.CANCELED);
        ParticipationRequest canceledRequest = requestRepository.save(request);
        log.info("Запрос id={} отменен", requestId);

        return requestMapper.toDto(canceledRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipationRequests(Long userId, Long eventId) {
        log.info("Получение запросов на участие в событии id={} пользователя id={}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " у пользователя id=" + userId + " не найдено"));

        List<ParticipationRequest> requests = requestRepository.findByEventInitiatorIdAndEventId(userId, eventId);
        return requestMapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.info("Обновление статуса запросов для события id={} пользователем id={}: {}",
                eventId, userId, updateRequest);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " у пользователя id=" + userId + " не найдено"));

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ValidationException("Для данного события подтверждение заявок не требуется");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(updateRequest.getRequestIds());

        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Запрос id=" + request.getId() + " не принадлежит событию id=" + eventId);
            }
        }

        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != ParticipationStatus.PENDING) {
                throw new ConflictException("Статус можно изменить только у заявок в состоянии PENDING");
            }

            if (updateRequest.getStatus().equals("CONFIRMED")) {
                if (event.getParticipantLimit() > 0 &&
                    event.getConfirmedRequests() >= event.getParticipantLimit()) {
                    throw new ConflictException("Достигнут лимит участников для события id=" + eventId);
                }

                request.setStatus(ParticipationStatus.CONFIRMED);
                confirmedRequests.add(request);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);

                if (event.getParticipantLimit() > 0 &&
                    event.getConfirmedRequests() >= event.getParticipantLimit()) {
                    rejectPendingRequests(eventId, updateRequest.getRequestIds());
                }
            } else if (updateRequest.getStatus().equals("REJECTED")) {
                request.setStatus(ParticipationStatus.REJECTED);
                rejectedRequests.add(request);
            }
        }

        eventRepository.save(event);
        requestRepository.saveAll(requests);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(requestMapper.toDtoList(confirmedRequests));
        result.setRejectedRequests(requestMapper.toDtoList(rejectedRequests));

        log.info("Статусы запросов обновлены для события id={}: подтверждено={}, отклонено={}",
                eventId, confirmedRequests.size(), rejectedRequests.size());

        return result;
    }

    private void rejectPendingRequests(Long eventId, List<Long> processedRequestIds) {
        List<ParticipationRequest> pendingRequests = requestRepository.findByEventId(eventId).stream()
                .filter(request -> request.getStatus() == ParticipationStatus.PENDING)
                .filter(request -> !processedRequestIds.contains(request.getId()))
                .collect(Collectors.toList());

        for (ParticipationRequest request : pendingRequests) {
            request.setStatus(ParticipationStatus.REJECTED);
        }

        requestRepository.saveAll(pendingRequests);
        log.info("Отклонены {} ожидающих запросов для события id={} (лимит исчерпан)",
                pendingRequests.size(), eventId);
    }
}