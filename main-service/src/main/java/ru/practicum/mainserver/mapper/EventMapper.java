package ru.practicum.mainserver.mapper;

import org.mapstruct.*;
import ru.practicum.mainserver.dto.event.*;
import ru.practicum.mainserver.model.Event;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "location", source = "location")
    Event toEntity(NewEventDto newEventDto);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "state", source = "state", qualifiedByName = "mapEventStateToString")
    EventFullDto toFullDto(Event event);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    EventShortDto toShortDto(Event event);

    List<EventFullDto> toFullDtoList(List<Event> events);

    List<EventShortDto> toShortDtoList(List<Event> events);

    @Named("mapEventStateToString")
    default String mapEventStateToString(EventState state) {
        return state != null ? state.name() : null;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    void updateEventFromAdminRequest(UpdateEventAdminRequest updateRequest, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    void updateEventFromUserRequest(UpdateEventUserRequest updateRequest, @MappingTarget Event event);

    default void updateEventFieldsFromAdminRequest(UpdateEventAdminRequest updateRequest, Event event) {
        if (updateRequest == null) {
            return;
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.getLocation() != null) {
            event.setLocation(null);
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
    }

    default void updateEventFieldsFromUserRequest(UpdateEventUserRequest updateRequest, Event event) {
        if (updateRequest == null) {
            return;
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.getLocation() != null) {
            event.setLocation(null);
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
    }
}