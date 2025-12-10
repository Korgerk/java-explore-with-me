package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.ewm.dto.EventCreateDto;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.model.Event;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class, CategoryMapper.class}
)
public interface EventMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    Event toEvent(EventCreateDto eventCreateDto);

    @Mapping(target = "initiator", qualifiedByName = "toUserShortDto")
    @Mapping(target = "category", qualifiedByName = "toCategoryDto")
    EventFullDto toEventFullDto(Event event, Long views);

    @Named("toEventShortDto")
    EventShortDto toEventShortDto(Event event);
}