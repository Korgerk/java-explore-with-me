package ru.practicum.ewm.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

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