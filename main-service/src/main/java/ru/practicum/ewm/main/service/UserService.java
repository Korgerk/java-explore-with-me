package ru.practicum.ewm.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.UserDto;
import ru.practicum.ewm.main.dto.NewUserRequest;
import ru.practicum.ewm.main.mapper.UserMapper;
import ru.practicum.ewm.main.model.User;
import ru.practicum.ewm.main.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto create(NewUserRequest newUserRequest) {
        User user = UserMapper.toEntity(newUserRequest);
        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        List<User> users = ids == null || ids.isEmpty()
                ? userRepository.findAll(from, size)
                : userRepository.findByIds(ids);
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }
}