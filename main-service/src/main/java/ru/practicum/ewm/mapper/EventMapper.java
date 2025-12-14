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

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public EventMapper(CategoryMapper categoryMapper, UserMapper userMapper) {
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
    }

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

        boolean paid = dto.getPaid() != null && dto.getPaid();
        int participantLimit = dto.getParticipantLimit() == null ? 0 : dto.getParticipantLimit();
        boolean requestModeration = dto.getRequestModeration() == null || dto.getRequestModeration();

        event.setPaid(paid);
        event.setParticipantLimit(participantLimit);
        event.setRequestModeration(requestModeration);

        Location location = new Location();
        location.setLat(dto.getLocation().getLat());
        location.setLon(dto.getLocation().getLon());
        event.setLocation(location);

        return event;
    }

    public EventShortDto toShortDto(Event event, long confirmedRequests, long views) {
        if (event == null) {
            return null;
        }
        return new EventShortDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                categoryMapper.toDto(event.getCategory()),
                userMapper.toDto(event.getInitiator()),
                event.getEventDate(),
                event.isPaid(),
                confirmedRequests,
                views
        );
    }

    public EventFullDto toFullDto(Event event, long confirmedRequests, long views) {
        if (event == null) {
            return null;
        }

        LocationDto locationDto = null;
        if (event.getLocation() != null) {
            locationDto = new LocationDto();
            locationDto.setLat(event.getLocation().getLat());
            locationDto.setLon(event.getLocation().getLon());
        }

        String state = event.getState() == null ? null : event.getState().name();

        return new EventFullDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                categoryMapper.toDto(event.getCategory()),
                event.isPaid(),
                event.getEventDate(),
                userMapper.toDto(event.getInitiator()),
                event.getDescription(),
                event.getParticipantLimit(),
                state,
                event.getCreatedOn(),
                event.getPublishedOn(),
                locationDto,
                event.isRequestModeration(),
                views,
                confirmedRequests
        );
    }
}
