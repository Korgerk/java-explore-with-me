package ru.practicum.ewm.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.UserCreateDto;
import ru.practicum.ewm.dto.UserDto;


import java.util.List;

public interface UserService {

    UserDto addUser(UserCreateDto userCreateDto);

    void removeUser(long userId);

    List<UserDto> getUsers(List<Long> ids, Pageable pageable);
}