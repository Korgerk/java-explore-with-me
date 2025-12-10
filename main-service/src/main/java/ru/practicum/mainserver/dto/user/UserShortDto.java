package ru.practicum.mainserver.dto.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserShortDto {
    @NotNull(message = "ID пользователя не может быть null")
    Long id;

    @NotBlank(message = "Имя не может быть пустым")
    String name;
}