package ru.practicum.ewm.model;

import javax.persistence.*;

import lombok.*;

@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SequenceGenerator(
        name = "id_gen",
        sequenceName = "cat_seq",
        allocationSize = 1)
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_gen")
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

}