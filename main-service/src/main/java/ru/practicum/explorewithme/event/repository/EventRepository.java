package ru.practicum.explorewithme.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    List<Event> findByIdIn(List<Long> ids, Pageable pageable);

    List<Event> findByCategoryIdIn(List<Long> catIds, Pageable pageable);

    @Query("SELECT e FROM Event e " +
           "WHERE (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
           "AND (:categories IS NULL OR e.category.id IN :categories) " +
           "AND (:paid IS NULL OR e.paid = :paid) " +
           "AND e.eventDate >= :rangeStart " +
           "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
           "AND e.state = 'PUBLISHED'")
    List<Event> searchEvents(@Param("text") String text,
                             @Param("categories") List<Long> categories,
                             @Param("paid") Boolean paid,
                             @Param("rangeStart") LocalDateTime rangeStart,
                             @Param("rangeEnd") LocalDateTime rangeEnd,
                             Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.state IN :states")
    List<Event> findByStateIn(@Param("states") List<EventState> states, Pageable pageable);

    @Query("SELECT e FROM Event e " +
           "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
           "AND (:states IS NULL OR e.state IN :states) " +
           "AND (:categories IS NULL OR e.category.id IN :categories) " +
           "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
           "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    List<Event> findForAdmin(@Param("users") List<Long> users,
                             @Param("states") List<EventState> states,
                             @Param("categories") List<Long> categories,
                             @Param("rangeStart") LocalDateTime rangeStart,
                             @Param("rangeEnd") LocalDateTime rangeEnd,
                             Pageable pageable);

    boolean existsByCategoryId(Long catId);
}