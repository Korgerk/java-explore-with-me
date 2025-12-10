package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.Location;
import ru.practicum.ewm.main.model.LocationEntity;

public class LocationMapper {

    public static LocationEntity toEntity(Location dto) {
        if (dto == null) return null;
        return LocationEntity.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    public static Location toDto(LocationEntity entity) {
        if (entity == null) return null;
        return new Location(entity.getLat(), entity.getLon());
    }
}