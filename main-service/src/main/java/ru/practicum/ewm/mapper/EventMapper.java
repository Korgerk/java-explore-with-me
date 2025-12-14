package ru.practicum.ewm.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.LocationDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;

@Component
public class EventMapper {

    public Event toEntity(NewEventDto dto, User initiator, Category category) {
        if (dto == null) {
            return null;
        }

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setEventDate(dto.getEventDate());
        event.setPaid(Boolean.TRUE.equals(dto.getPaid()));
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration() == null || dto.getRequestModeration());

        Location location = new Location();
        if (dto.getLocation() != null) {
            location.setLat(dto.getLocation().getLat());
            location.setLon(dto.getLocation().getLon());
        }
        event.setLocation(location);

        return event;
    }

    public EventShortDto toShortDto(Event event, long confirmedRequests, long views) {
        if (event == null) {
            return null;
        }

        Long categoryId = event.getCategory() == null ? null : event.getCategory().getId();
        Long initiatorId = event.getInitiator() == null ? null : event.getInitiator().getId();

        return new EventShortDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                categoryId,
                initiatorId,
                event.isPaid(),
                event.getEventDate(),
                confirmedRequests,
                views
        );
    }

    public EventFullDto toFullDto(Event event, long confirmedRequests, long views) {
        if (event == null) {
            return null;
        }

        Long categoryId = event.getCategory() == null ? null : event.getCategory().getId();
        Long initiatorId = event.getInitiator() == null ? null : event.getInitiator().getId();

        LocationDto locationDto = null;
        if (event.getLocation() != null) {
            locationDto = new LocationDto(
                    event.getLocation().getLat(),
                    event.getLocation().getLon()
            );
        }

        String state = event.getState() == null ? null : event.getState().name();

        return new EventFullDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                event.getDescription(),
                categoryId,
                initiatorId,
                event.isPaid(),
                event.getParticipantLimit(),
                event.isRequestModeration(),
                event.getEventDate(),
                event.getCreatedOn(),
                event.getPublishedOn(),
                state,
                confirmedRequests,
                views,
                locationDto
        );
    }
}
