package ru.practicum.ewm.main.model;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "users", indexes = {
        @Index(columnList = "email", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 250)
    private String name;

    @Column(nullable = false, length = 254, unique = true)
    private String email;
}