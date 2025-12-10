package ru.practicum.ewm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CompilationCreateDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

    private Set<Long> events;

    @Builder.Default
    private Boolean pinned = false;

}