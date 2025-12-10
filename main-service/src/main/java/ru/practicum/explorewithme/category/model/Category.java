package ru.practicum.explorewithme.category.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "categories", schema = "public", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;
}