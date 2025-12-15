package ru.practicum.ewm.service.rating;

import ru.practicum.ewm.dto.rating.EventRatingSummaryDto;
import ru.practicum.ewm.dto.rating.RatingRequestDto;

public interface RatingService {

    void rateEvent(Long userId, Long eventId, RatingRequestDto dto);

    void deleteRating(Long userId, Long eventId);

    EventRatingSummaryDto getEventRating(Long eventId);
}
