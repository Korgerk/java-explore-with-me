package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.dto.UserCreateDto;
import ru.practicum.ewm.dto.UserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreateDto userCreateDto);

    UserDto toUserDto(User user);

    @Named("toUserShortDto")
    UserShortDto toUserShortDto(User user);

}