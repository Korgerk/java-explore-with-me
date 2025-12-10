package ru.practicum.mainserver.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainserver.exception.ConflictException;
import ru.practicum.mainserver.exception.NotFoundException;
import ru.practicum.mainserver.exception.ValidationException;
import ru.practicum.mainserver.model.User;
import ru.practicum.mainserver.repository.UserRepository;
import ru.practicum.mainserver.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User createUser(User user) {
        log.info("Создание пользователя: {}", user);

        // Проверка уникальности email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Пользователь с email=" + user.getEmail() + " уже существует");
        }

        User savedUser = userRepository.save(user);
        log.info("Пользователь создан с id: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с id: {}", userId);

        // Проверка существования пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        userRepository.delete(user);
        log.info("Пользователь с id={} удален", userId);
    }

    @Override
    public List<User> getUsers(List<Long> ids, int from, int size) {
        log.info("Получение пользователей: ids={}, from={}, size={}", ids, from, size);

        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(pageable).getContent();
        } else {
            return userRepository.findByIdIn(ids, pageable).getContent();
        }
    }

    @Override
    public User getUser(Long userId) {
        log.info("Получение пользователя с id: {}", userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }
}