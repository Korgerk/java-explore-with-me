package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("select r.event.id, count(r) from ParticipationRequest r " +
           "where r.event.id in :eventIds and r.status = :status group by r.event.id")
    List<Object[]> countByEventIdsAndStatusGrouped(
            @Param("eventIds") List<Long> eventIds,
            @Param("status") RequestStatus status
    );

    Optional<ParticipationRequest> findByIdAndRequesterId(Long id, Long requesterId);
}
