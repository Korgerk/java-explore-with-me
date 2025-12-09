package ru.practicum.explorewithme.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.dto.EventFullDto;
import ru.practicum.explorewithme.dto.EventShortDto;
import ru.practicum.explorewithme.dto.LocationDto;
import ru.practicum.explorewithme.dto.NewEventDto;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.Location;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    EventShortDto toEventShortDto(Event event);

    Location toLocation(LocationDto locationDto);

    LocationDto toLocationDto(Location location);
}