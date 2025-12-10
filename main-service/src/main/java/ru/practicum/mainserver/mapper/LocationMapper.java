package ru.practicum.mainserver.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.mainserver.dto.location.Location;
import ru.practicum.mainserver.model.LocationEntity;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    LocationEntity toEntity(Location location);

    Location toDto(LocationEntity locationEntity);
}