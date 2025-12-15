package ru.practicum.ewm.service.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.rating.EventRatingSummaryDto;
import ru.practicum.ewm.dto.rating.RatingRequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventRating;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.model.enums.RatingValue;
import ru.practicum.ewm.repository.EventRatingRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingServiceImpl implements RatingService {

    private final EventRatingRepository ratingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public void rateEvent(Long userId, Long eventId, RatingRequestDto dto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event is not published");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Cannot rate own event");
        }

        EventRating rating = ratingRepository
                .findByEventIdAndUserId(eventId, userId)
                .orElse(new EventRating(null, event, user, dto.getValue()));

        rating.setValue(dto.getValue());
        ratingRepository.save(rating);
    }

    @Override
    public void deleteRating(Long userId, Long eventId) {
        EventRating rating = ratingRepository
                .findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

        ratingRepository.delete(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public EventRatingSummaryDto getEventRating(Long eventId) {
        long likes = ratingRepository.countByEventIdAndValue(eventId, RatingValue.LIKE);
        long dislikes = ratingRepository.countByEventIdAndValue(eventId, RatingValue.DISLIKE);
        return new EventRatingSummaryDto(likes, dislikes, likes - dislikes);
    }
}
