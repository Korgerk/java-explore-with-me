package ru.practicum.ewm.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.LocationDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;

@Component
public class EventMapper {

    public Event toEntity(NewEventDto dto, User initiator, Category category) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setPaid(dto.isPaid());
        event.setRequestModeration(dto.isRequestModeration());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setInitiator(initiator);
        event.setCategory(category);

        if (dto.getLocation() != null) {
            event.setLat(dto.getLocation().getLat());
            event.setLon(dto.getLocation().getLon());
        }

        return event;
    }

    public EventFullDto toFullDto(Event event, long confirmed, long views) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setState(event.getState() == null ? null : event.getState().name());
        dto.setPaid(event.isPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.isRequestModeration());
        dto.setCategory(event.getCategory() == null ? null : event.getCategory().getId());
        dto.setInitiator(event.getInitiator() == null ? null : event.getInitiator().getId());
        dto.setConfirmedRequests(confirmed);
        dto.setViews(views);

        LocationDto location = new LocationDto(event.getLat(), event.getLon());
        dto.setLocation(location);

        return dto;
    }

    public EventShortDto toShortDto(Event event, long confirmed, long views) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setEventDate(event.getEventDate());
        dto.setPaid(event.isPaid());
        dto.setCategory(event.getCategory() == null ? null : event.getCategory().getId());
        dto.setInitiator(event.getInitiator() == null ? null : event.getInitiator().getId());
        dto.setConfirmedRequests(confirmed);
        dto.setViews(views);
        return dto;
    }
}
