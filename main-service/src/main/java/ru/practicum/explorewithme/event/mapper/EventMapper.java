package ru.practicum.explorewithme.event.mapper;

import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.event.Location;
import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class EventMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event toEvent(NewEventDto dto, ru.practicum.explorewithme.user.model.User initiator,
                                ru.practicum.explorewithme.category.model.Category category) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setPaid(dto.getPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(dto.getLocation());
        event.setCreatedOn(LocalDateTime.now());
        return event;
    }

    public static EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        dto.setEventDate(FORMATTER.format(event.getEventDate()));
        dto.setPaid(event.getPaid());
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(views);
        return dto;
    }

    public static EventShortDto toEventShortDtoNoStats(Event event) {
        return toEventShortDto(event, 0L, 0L);
    }

    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        dto.setLocation(event.getLocation());
        dto.setEventDate(FORMATTER.format(event.getEventDate()));
        dto.setCreatedOn(FORMATTER.format(event.getCreatedOn()));
        if (event.getPublishedOn() != null) {
            dto.setPublishedOn(FORMATTER.format(event.getPublishedOn()));
        }
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState().name());
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(views);
        return dto;
    }

    public static EventFullDto toEventFullDto(Event event) {
        return toEventFullDto(event, 0L, 0L);
    }

    public static Event partialUpdate(Event event, UpdateEventUserRequest dto) {
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getCategory() != null) {
            ru.practicum.explorewithme.category.model.Category cat = new ru.practicum.explorewithme.category.model.Category();
            cat.setId(dto.getCategory());
            event.setCategory(cat);
        }
        if (dto.getEventDate() != null) event.setEventDate(dto.getEventDate());
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        return event;
    }

    public static Event partialUpdate(Event event, UpdateEventAdminRequest dto) {
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getCategory() != null) {
            ru.practicum.explorewithme.category.model.Category cat = new ru.practicum.explorewithme.category.model.Category();
            cat.setId(dto.getCategory());
            event.setCategory(cat);
        }
        if (dto.getEventDate() != null) event.setEventDate(dto.getEventDate());
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        return event;
    }
}