package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.NewUserRequest;
import ru.practicum.ewm.main.dto.UserDto;
import ru.practicum.ewm.main.dto.UserShortDto;
import ru.practicum.ewm.main.model.User;

public class UserMapper {

    public static User toEntity(NewUserRequest dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public static UserShortDto toShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }
}