package ru.practicum.ewm.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.LocationDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;

@Component
public class EventMapper {

    public Event toEntity(NewEventDto dto, ru.practicum.ewm.model.User initiator, ru.practicum.ewm.model.Category category) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setCategory(category);
        event.setPaid(dto.isPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.isRequestModeration());
        event.setEventDate(dto.getEventDate());
        event.setInitiator(initiator);

        Location location = new Location();
        location.setLat(dto.getLocation().getLat());
        location.setLon(dto.getLocation().getLon());
        event.setLocation(location);

        return event;
    }

    public EventFullDto toFullDto(Event event, long confirmed, long views) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setPaid(event.isPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.isRequestModeration());
        dto.setConfirmedRequests(confirmed);
        dto.setViews(views);
        dto.setState(event.getState().name());

        LocationDto locationDto = new LocationDto();
        locationDto.setLat(event.getLocation().getLat());
        locationDto.setLon(event.getLocation().getLon());
        dto.setLocation(locationDto);

        return dto;
    }

    public EventShortDto toShortDto(Event event, long confirmed, long views) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setEventDate(event.getEventDate());
        dto.setPaid(event.isPaid());
        dto.setConfirmedRequests(confirmed);
        dto.setViews(views);
        return dto;
    }
}
