package ru.practicum.mainserver.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.mainserver.dto.event.EventState;
import ru.practicum.mainserver.dto.participation.ParticipationStatus;
import ru.practicum.mainserver.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MapperUtil {

    public String mapEventState(EventState state) {
        return state != null ? state.toString() : null;
    }

    public EventState mapEventState(String state) {
        return state != null ? EventState.valueOf(state) : null;
    }

    public String mapParticipationStatus(ParticipationStatus status) {
        return status != null ? status.toString() : null;
    }

    public ParticipationStatus mapParticipationStatus(String status) {
        return status != null ? ParticipationStatus.valueOf(status) : null;
    }

    public LocalDateTime mapTimestamp(String timestamp) {
        return timestamp != null ? LocalDateTime.parse(timestamp) : null;
    }

    public String mapTimestamp(LocalDateTime timestamp) {
        return timestamp != null ? timestamp.toString() : null;
    }

    public List<Event> mapEventIdsToEvents(Set<Long> eventIds) {
        return List.of();
    }

    public Set<Long> mapEventsToIds(List<Event> events) {
        return events.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());
    }
}