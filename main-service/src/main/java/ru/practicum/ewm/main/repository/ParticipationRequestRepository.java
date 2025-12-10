package ru.practicum.ewm.main.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.main.model.ParticipationRequest;
import ru.practicum.ewm.main.model.RequestStatus;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ParticipationRequestRepository {

    private final EntityManager em;

    public ParticipationRequest save(ParticipationRequest request) {
        if (request.getId() == null) {
            em.persist(request);
        } else {
            em.merge(request);
        }
        return request;
    }

    public List<ParticipationRequest> findByRequesterId(Long userId) {
        return em.createQuery("SELECT r FROM ParticipationRequest r WHERE r.requester.id = :userId", ParticipationRequest.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<ParticipationRequest> findByEventId(Long eventId) {
        return em.createQuery("SELECT r FROM ParticipationRequest r WHERE r.event.id = :eventId", ParticipationRequest.class)
                .setParameter("eventId", eventId)
                .getResultList();
    }

    public long countConfirmedByEventId(Long eventId) {
        return em.createQuery("SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event.id = :eventId AND r.status = :status", Long.class)
                .setParameter("eventId", eventId)
                .setParameter("status", RequestStatus.CONFIRMED)
                .getSingleResult();
    }

    public Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId) {
        return em.createQuery("SELECT r FROM ParticipationRequest r WHERE r.event.id = :eventId AND r.requester.id = :requesterId", ParticipationRequest.class)
                .setParameter("eventId", eventId)
                .setParameter("requesterId", requesterId)
                .getResultList()
                .stream()
                .findFirst();
    }

    public List<ParticipationRequest> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return em.createQuery("SELECT r FROM ParticipationRequest r WHERE r.id IN :ids", ParticipationRequest.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    public Optional<ParticipationRequest> findById(Long id) {
        return Optional.ofNullable(em.find(ParticipationRequest.class, id));
    }

    public List<ru.practicum.ewm.main.model.ParticipationRequest> saveAll(List<ru.practicum.ewm.main.model.ParticipationRequest> requests) {
        return requests.stream().map(this::save).collect(Collectors.toList());
    }

    public List<ru.practicum.ewm.main.model.ParticipationRequest> findConfirmedByEventIds(List<Long> eventIds) {
        if (eventIds.isEmpty()) return List.of();
        return em.createQuery(
                        "SELECT r FROM ParticipationRequest r WHERE r.event.id IN :ids AND r.status = :status",
                        ru.practicum.ewm.main.model.ParticipationRequest.class
                )
                .setParameter("ids", eventIds)
                .setParameter("status", RequestStatus.CONFIRMED)
                .getResultList();
    }
}