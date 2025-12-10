package ru.practicum.ewm.main.model;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "categories", indexes = {
        @Index(columnList = "name", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;
}