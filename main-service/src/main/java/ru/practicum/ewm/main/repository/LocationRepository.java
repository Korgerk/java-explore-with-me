package ru.practicum.ewm.main.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.main.model.LocationEntity;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class LocationRepository {

    private final EntityManager em;

    public LocationEntity save(LocationEntity location) {
        if (location.getId() == null) {
            em.persist(location);
        } else {
            em.merge(location);
        }
        return location;
    }
}