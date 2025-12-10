package ru.practicum.mainserver.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.practicum.mainserver.dto.event.EventState;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "events")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "Аннотация не может быть пустой")
    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов")
    @Column(name = "annotation", nullable = false, length = 2000)
    String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    Category category;

    @Column(name = "confirmed_requests", nullable = false)
    @Builder.Default
    Integer confirmedRequests = 0;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов")
    @Column(name = "description", nullable = false, length = 7000)
    String description;

    @NotNull(message = "Дата события не может быть null")
    @Future(message = "Дата события должна быть в будущем")
    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    @ToString.Exclude
    User initiator;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", nullable = false)
    @ToString.Exclude
    LocationEntity location;

    @Column(name = "paid", nullable = false)
    @Builder.Default
    Boolean paid = false;

    @Column(name = "participant_limit", nullable = false)
    @Builder.Default
    Integer participantLimit = 0;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "request_moderation", nullable = false)
    @Builder.Default
    Boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    @Builder.Default
    EventState state = EventState.PENDING;

    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов")
    @Column(name = "title", nullable = false, length = 120)
    String title;

    @Column(name = "views", nullable = false)
    @Builder.Default
    Long views = 0L;

    @PrePersist
    protected void onCreate() {
        createdOn = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}