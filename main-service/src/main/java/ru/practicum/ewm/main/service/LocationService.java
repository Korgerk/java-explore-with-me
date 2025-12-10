package ru.practicum.ewm.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.Location;
import ru.practicum.ewm.main.mapper.LocationMapper;
import ru.practicum.ewm.main.model.LocationEntity;
import ru.practicum.ewm.main.repository.LocationRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;

    @Transactional
    public LocationEntity getOrCreate(Location dto) {
        if (dto == null) {
            return null;
        }
        LocationEntity entity = LocationMapper.toEntity(dto);
        return locationRepository.save(entity);
    }
}