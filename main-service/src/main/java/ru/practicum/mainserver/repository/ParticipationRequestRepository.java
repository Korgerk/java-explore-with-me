package ru.practicum.mainserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.mainserver.dto.participation.ParticipationStatus;
import ru.practicum.mainserver.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByRequesterId(Long userId);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByEventInitiatorIdAndEventId(Long userId, Long eventId);

    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long userId, Long eventId);

    long countByEventIdAndStatus(Long eventId, ParticipationStatus status);

    @Query("SELECT pr FROM ParticipationRequest pr " +
           "WHERE pr.event.id = :eventId " +
           "AND pr.status = 'CONFIRMED'")
    List<ParticipationRequest> findConfirmedRequests(@Param("eventId") Long eventId);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);
}