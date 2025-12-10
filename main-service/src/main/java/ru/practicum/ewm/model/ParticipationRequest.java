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
        sequenceName = "req_seq",
        allocationSize = 1)
@Table(name = "requests")
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private RequestStatus status;

    @Column(name = "created")
    private LocalDateTime created;

}