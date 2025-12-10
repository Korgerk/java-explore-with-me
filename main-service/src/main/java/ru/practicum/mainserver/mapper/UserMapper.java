package ru.practicum.mainserver.mapper;

import org.mapstruct.*;
import ru.practicum.mainserver.dto.user.NewUserRequest;
import ru.practicum.mainserver.dto.user.UserDto;
import ru.practicum.mainserver.dto.user.UserShortDto;
import ru.practicum.mainserver.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest newUserRequest);

    User toEntity(UserDto userDto);

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);

    List<UserDto> toDtoList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserDto userDto, @MappingTarget User user);
}