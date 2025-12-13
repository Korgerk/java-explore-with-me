package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.location.Location;
import ru.practicum.ewm.model.LocationEntity;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    LocationEntity toEntity(Location location);

    Location toDto(LocationEntity locationEntity);
}