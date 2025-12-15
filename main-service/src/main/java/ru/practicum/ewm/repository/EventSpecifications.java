package ru.practicum.ewm.repository;

import jakarta.persistence.criteria.Expression;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class EventSpecifications {

    static Specification<Event> hasState(EventState state) {
        return (root, query, cb) -> cb.equal(root.get("state"), state);
    }

    static Specification<Event> initiatorIn(List<Long> userIds) {
        return (root, query, cb) -> root.get("initiator").get("id").in(userIds);
    }

    static Specification<Event> categoryIn(List<Long> categoryIds) {
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    static Specification<Event> stateIn(List<EventState> states) {
        return (root, query, cb) -> root.get("state").in(states);
    }

    static Specification<Event> dateAfter(LocalDateTime start) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), start);
    }

    static Specification<Event> dateBefore(LocalDateTime end) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), end);
    }

    static Specification<Event> textLike(String text) {
        return (root, query, cb) -> {
            String like = "%" + text.toLowerCase() + "%";
            Expression<String> ann = cb.lower(root.get("annotation"));
            Expression<String> desc = cb.lower(root.get("description"));
            return cb.or(cb.like(ann, like), cb.like(desc, like));
        };
    }

    static Specification<Event> paidIs(Boolean paid) {
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }
}
