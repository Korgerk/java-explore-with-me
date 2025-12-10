package ru.practicum.mainserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.mainserver.model.LocationEntity;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
}