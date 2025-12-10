package ru.practicum.ewm.model;

import javax.persistence.*;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SequenceGenerator(
        name = "id_gen",
        sequenceName = "comp_seq",
        allocationSize = 1)
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_gen")
    private Long id;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @ManyToMany
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events;

    @Column(name = "pinned", nullable = false)
    private Boolean pinned;

}