package ru.practicum.explorewithme.event.model;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.event.Location;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events", schema = "public")
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private Boolean paid = false;

    @Column(nullable = false)
    private Integer participantLimit = 0;

    @Column(nullable = false)
    private Boolean requestModeration = true;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime publishedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventState state = EventState.PENDING;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Embedded
    private Location location;
}