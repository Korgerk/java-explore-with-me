package ru.practicum.ewm.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.enums.EventState;

import jakarta.persistence.criteria.Expression;
import java.time.LocalDateTime;
import java.util.List;

public class EventSpecifications {

    public static Specification<Event> hasState(EventState state) {
        return (root, query, cb) -> cb.equal(root.get("state"), state);
    }

    public static Specification<Event> initiatorIn(List<Long> userIds) {
        return (root, query, cb) -> root.get("initiator").get("id").in(userIds);
    }

    public static Specification<Event> categoryIn(List<Long> categoryIds) {
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    public static Specification<Event> stateIn(List<EventState> states) {
        return (root, query, cb) -> root.get("state").in(states);
    }

    public static Specification<Event> dateAfter(LocalDateTime start) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), start);
    }

    public static Specification<Event> dateBefore(LocalDateTime end) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), end);
    }

    public static Specification<Event> textLike(String text) {
        return (root, query, cb) -> {
            String like = "%" + text.toLowerCase() + "%";
            Expression<String> ann = cb.lower(root.get("annotation"));
            Expression<String> desc = cb.lower(root.get("description"));
            return cb.or(cb.like(ann, like), cb.like(desc, like));
        };
    }

    public static Specification<Event> paidIs(Boolean paid) {
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }
}
