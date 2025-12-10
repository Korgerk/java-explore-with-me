package ru.practicum.explorewithme.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.request.model.ParticipationRequest;
import ru.practicum.explorewithme.request.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findByRequesterId(Long userId);
    List<ParticipationRequest> findByEventId(Long eventId);
    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, RequestStatus status);
    long countByEventIdAndStatus(Long eventId, RequestStatus status);
    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);
}