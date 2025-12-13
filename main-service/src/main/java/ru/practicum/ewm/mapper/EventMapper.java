package ru.practicum.ewm.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
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
        event.setPaid(dto.isPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.isRequestModeration());

        Location location = new Location();
        location.setLat(dto.getLat());
        location.setLon(dto.getLon());
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

        double lat = 0.0;
        double lon = 0.0;
        if (event.getLocation() != null) {
            if (event.getLocation().getLat() != null) {
                lat = event.getLocation().getLat();
            }
            if (event.getLocation().getLon() != null) {
                lon = event.getLocation().getLon();
            }
        }

        String state = event.getState() == null ? null : event.getState().name();

        return new EventFullDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                event.getDescription(),
                categoryMapper.toDto(event.getCategory()),
                userMapper.toDto(event.getInitiator()),
                event.getEventDate(),
                event.getCreatedOn(),
                event.getPublishedOn(),
                event.isPaid(),
                event.getParticipantLimit(),
                event.isRequestModeration(),
                state,
                lat,
                lon,
                confirmedRequests,
                views
        );
    }
}
