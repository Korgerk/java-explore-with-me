package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.model.Request;
import ru.practicum.explorewithme.model.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterId(Long userId);

    List<Request> findAllByEventId(Long eventId);

    @Query("SELECT r FROM Request r WHERE r.id IN :ids")
    List<Request> findByIdIn(@Param("ids") List<Long> ids);

    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long userId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    Boolean existsByEventIdAndRequesterId(Long eventId, Long userId);
}