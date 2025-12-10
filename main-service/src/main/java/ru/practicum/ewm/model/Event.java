package ru.practicum.ewm.model;

import javax.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SequenceGenerator(
        name = "id_gen",
        sequenceName = "event_seq",
        allocationSize = 1)
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_gen")
    private Long id;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;

    @Column(name = "description", nullable = false, length = 7000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "confirmed_requests")
    private int confirmedRequests;

    @Embedded
    private Location location;

    @Column(name = "paid", nullable = false)
    private boolean paid;

    @Column(name = "participant_limit")
    private int participantLimit;

    @Column(name = "request_moderation", nullable = false)
    private boolean requestModeration;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private EventState state;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

}