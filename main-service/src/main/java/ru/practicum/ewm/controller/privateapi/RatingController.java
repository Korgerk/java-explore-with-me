package ru.practicum.ewm.controller.privateapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.rating.EventRatingSummaryDto;
import ru.practicum.ewm.dto.rating.RatingRequestDto;
import ru.practicum.ewm.service.rating.RatingService;

import static ru.practicum.ewm.util.ApiPaths.PRIVATE_EVENT_RATINGS;
import static ru.practicum.ewm.util.ApiPaths.SUMMARY;

@RestController
@RequiredArgsConstructor
@RequestMapping(PRIVATE_EVENT_RATINGS)
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void rate(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody @Valid RatingRequestDto dto
    ) {
        ratingService.rateEvent(userId, eventId, dto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        ratingService.deleteRating(userId, eventId);
    }

    @GetMapping(SUMMARY)
    public EventRatingSummaryDto getSummary(@PathVariable Long eventId) {
        return ratingService.getEventRating(eventId);
    }
}
