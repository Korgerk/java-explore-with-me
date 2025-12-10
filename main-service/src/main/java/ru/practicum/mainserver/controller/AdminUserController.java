package ru.practicum.mainserver.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainserver.dto.user.NewUserRequest;
import ru.practicum.mainserver.dto.user.UserDto;
import ru.practicum.mainserver.mapper.UserMapper;
import ru.practicum.mainserver.model.User;
import ru.practicum.mainserver.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminUserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {

        log.info("Получение пользователей: ids={}, from={}, size={}", ids, from, size);

        List<User> users = userService.getUsers(ids, from, size);
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.info("Создание пользователя: {}", newUserRequest);
        User user = userMapper.toEntity(newUserRequest);
        User savedUser = userService.createUser(user);
        return userMapper.toDto(savedUser);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Удаление пользователя с id={}", userId);
        userService.deleteUser(userId);
    }
}