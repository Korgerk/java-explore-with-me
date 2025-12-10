package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.model.EventState;

public class EventMapper {

    public static Event toEntity(
            NewEventDto dto,
            ru.practicum.ewm.main.model.User initiator,
            ru.practicum.ewm.main.model.Category category,
            ru.practicum.ewm.main.model.LocationEntity location) {

        return Event.builder()
                .title(dto.getTitle())
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate()) // ← уже LocalDateTime
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .initiator(initiator)
                .category(category)
                .location(location)
                .build();
    }

    public static EventShortDto toShortDto(Event event, Long views, Long confirmedRequests) {
        return new EventShortDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                CategoryMapper.toDto(event.getCategory()),
                UserMapper.toShortDto(event.getInitiator()),
                event.getPaid(),
                event.getEventDate(),
                views,
                confirmedRequests
        );
    }

    public static EventFullDto toFullDto(Event event, Long views, Long confirmedRequests) {
        return new EventFullDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                event.getDescription(),
                CategoryMapper.toDto(event.getCategory()),
                UserMapper.toShortDto(event.getInitiator()),
                LocationMapper.toDto(event.getLocation()),
                event.getPaid(),
                event.getEventDate(),
                event.getCreatedOn(),
                event.getPublishedOn(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getState(),
                views,
                confirmedRequests
        );
    }
}