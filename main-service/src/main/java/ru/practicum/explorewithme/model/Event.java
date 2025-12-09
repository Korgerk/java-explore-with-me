package ru.practicum.explorewithme.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.explorewithme.model.enums.EventState;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 120)
    String title;

    @Column(nullable = false, length = 2000)
    String annotation;

    @Column(length = 7000)
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    User initiator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    EventState state;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Embedded
    Location location;

    @Column(nullable = false)
    Boolean paid;

    @Column(name = "participant_limit", nullable = false)
    Integer participantLimit;

    @Column(name = "request_moderation", nullable = false)
    Boolean requestModeration;

    @Transient
    Long confirmedRequests;

    @Transient
    Long views;
}