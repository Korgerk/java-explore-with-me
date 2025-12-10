package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.RequestStatusUpdateDto;
import ru.practicum.ewm.dto.RequestStatusUpdateResult;
import ru.practicum.ewm.exception.ConflictDataException;
import ru.practicum.ewm.exception.EntityNotFoundException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(long userId, long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Указанный пользователь не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Указанное событие не найдено"));

        if (event.getInitiator().getId() == userId) {
            throw new ConflictDataException("Подача запроса на своё же событие");
        }

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictDataException("Повторный запрос");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictDataException("Событие еще не опубликовано");
        }

        var limit = event.getParticipantLimit();
        if (limit > 0 && limit <= requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED)) {
            throw new ConflictDataException("Сорян, но больше не влезет");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(event.isRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build();

        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        return requestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Указанный пользователь не найден"));

        return requestRepository.findByRequesterId(userId)
                .stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Указанный запрос не найден"));

        if (request.getRequester().getId() != userId) {
            throw new ForbiddenException("Отменять можно только свой запрос");
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            var event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(long userId, long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Указанное событие не найдено"));

        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenException("Запросы может просматривать только инициатор события");
        }

        return requestRepository.findByEventInitiatorIdAndEventId(userId, eventId)
                .stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public RequestStatusUpdateResult changeRequestStatus(long userId, long eventId, RequestStatusUpdateDto updateRequestDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Указанное событие ид=%s для пользователя ид=%s не найдено", eventId, userId)));

        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenException("Статус запросов может менять только инициатор события");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(updateRequestDto.getRequestIds());

        if (requests.stream()
                .anyMatch(req -> req.getEvent().getId() != eventId
                                 || req.getStatus() != RequestStatus.PENDING)) {
            throw new ConflictDataException("Не все запросы из списка относятся к событию или не в статусе PENDING");
        }

        var newStatus = updateRequestDto.getStatus();

        if (newStatus ==  RequestStatus.CONFIRMED
            && event.getConfirmedRequests() + requests.size() > event.getParticipantLimit()) {
            throw new ConflictDataException("не влезет");
        }

        requests.forEach(r -> r.setStatus(newStatus));
        requestRepository.saveAll(requests);

        RequestStatusUpdateResult result = new RequestStatusUpdateResult();
        if (newStatus == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
            eventRepository.save(event);

            result.getConfirmedRequests().addAll(requests.stream().map(requestMapper::toRequestDto).toList());
        } else if (newStatus == RequestStatus.REJECTED) {
            result.getRejectedRequests().addAll(requests.stream().map(requestMapper::toRequestDto).toList());
        } else {
            throw new ConflictDataException(String.format("Указан неверный статус %s", newStatus));
        }

        return result;
    }

}