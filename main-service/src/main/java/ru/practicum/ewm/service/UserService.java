package ru.practicum.ewm.service;

import ru.practicum.ewm.model.User;

import java.util.List;

public interface UserService {
    User createUser(User user);

    void deleteUser(Long userId);

    List<User> getUsers(List<Long> ids, int from, int size);

    User getUser(Long userId);
}