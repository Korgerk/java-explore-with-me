package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.EventRating;

import java.util.List;
import java.util.Optional;

public interface EventRatingRepository extends JpaRepository<EventRating, Long> {

    Optional<EventRating> findByEventIdAndUserId(Long eventId, Long userId);

    long countByEventIdAndValue(Long eventId, ru.practicum.ewm.model.enums.RatingValue value);

    List<EventRating> findByUserId(Long userId);
}
