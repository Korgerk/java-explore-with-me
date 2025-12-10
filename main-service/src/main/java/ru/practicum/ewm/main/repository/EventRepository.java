package ru.practicum.ewm.main.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.model.EventState;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventRepository {

    private final EntityManager em;

    public Event save(Event event) {
        if (event.getId() == null) {
            em.persist(event);
        } else {
            em.merge(event);
        }
        return event;
    }

    public Optional<Event> findById(Long id) {
        return Optional.ofNullable(em.find(Event.class, id));
    }

    public List<Event> findByInitiatorId(Long userId, int from, int size) {
        return em.createQuery("SELECT e FROM Event e WHERE e.initiator.id = :userId", Event.class)
                .setParameter("userId", userId)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
    }

    public Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId) {
        return em.createQuery("SELECT e FROM Event e WHERE e.id = :eventId AND e.initiator.id = :userId", Event.class)
                .setParameter("eventId", eventId)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .findFirst();
    }

    // Публичный поиск: только PUBLISHED + фильтры
    public List<Event> findPublicEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            int from,
            int size) {

        String jpql = "SELECT e FROM Event e WHERE e.state = :state";
        if (text != null && !text.isBlank()) {
            jpql += " AND (LOWER(e.annotation) LIKE LOWER(:text) OR LOWER(e.description) LIKE LOWER(:text))";
        }
        if (categories != null && !categories.isEmpty()) {
            jpql += " AND e.category.id IN :categories";
        }
        if (paid != null) {
            jpql += " AND e.paid = :paid";
        }
        jpql += " AND e.eventDate >= :rangeStart";
        if (rangeEnd != null) {
            jpql += " AND e.eventDate <= :rangeEnd";
        }
        if (onlyAvailable != null && onlyAvailable) {
            jpql += " AND (e.participantLimit = 0 OR e.participantLimit > (" +
                    "SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event.id = e.id AND r.status = 'CONFIRMED'))";
        }
        jpql += " ORDER BY e.eventDate ASC";

        TypedQuery<Event> query = em.createQuery(jpql, Event.class);
        query.setParameter("state", EventState.PUBLISHED);
        query.setParameter("rangeStart", rangeStart != null ? rangeStart : LocalDateTime.now());
        if (text != null && !text.isBlank()) {
            query.setParameter("text", "%" + text + "%");
        }
        if (categories != null && !categories.isEmpty()) {
            query.setParameter("categories", categories);
        }
        if (paid != null) {
            query.setParameter("paid", paid);
        }
        if (rangeEnd != null) {
            query.setParameter("rangeEnd", rangeEnd);
        }
        return query.setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
    }

    // Админский поиск
    public List<Event> findAdminEvents(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size) {

        String jpql = "SELECT e FROM Event e WHERE 1 = 1";
        if (users != null && !users.isEmpty()) jpql += " AND e.initiator.id IN :users";
        if (states != null && !states.isEmpty()) jpql += " AND e.state IN :states";
        if (categories != null && !categories.isEmpty()) jpql += " AND e.category.id IN :categories";
        if (rangeStart != null) jpql += " AND e.eventDate >= :rangeStart";
        if (rangeEnd != null) jpql += " AND e.eventDate <= :rangeEnd";

        TypedQuery<Event> query = em.createQuery(jpql, Event.class);
        if (users != null && !users.isEmpty()) query.setParameter("users", users);
        if (states != null && !states.isEmpty()) query.setParameter("states", states);
        if (categories != null && !categories.isEmpty()) query.setParameter("categories", categories);
        if (rangeStart != null) query.setParameter("rangeStart", rangeStart);
        if (rangeEnd != null) query.setParameter("rangeEnd", rangeEnd);

        return query.setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Event> findByIdIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return em.createQuery("SELECT e FROM Event e WHERE e.id IN :ids", Event.class)
                .setParameter("ids", ids)
                .getResultList();
    }
}