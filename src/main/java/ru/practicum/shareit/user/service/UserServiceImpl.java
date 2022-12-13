package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto add(UserDto userDto) {
        userStorage.getByEmail(userDto.getEmail()).ifPresent(
                foundUser -> {
                    throw new AlreadyExistsException("User with email=" + userDto.getEmail() + " already exists.");
                });
        User user = userStorage.add(UserMapper.fromUserDto(userDto));
        log.debug("UserServiceImpl: add user " + user + ".");
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.getAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.toUserDto(
                userStorage.getById(id)
                        .orElseThrow(
                                () -> new NotFoundException("User with id=" + id + " not found.")
                        )
        );
    }

    @Override
    public UserDto patch(Long id, UserDto patchDto) {
        User user = userStorage.getById(id)
                .orElseThrow(
                        () -> new NotFoundException("User with id=" + id + " not found.")
                );
        String patchEmail = patchDto.getEmail();
        if (Objects.nonNull(patchEmail)) {
            userStorage.getByEmailWithoutId(patchEmail, id).ifPresent(
                    foundUser -> {
                        throw new AlreadyExistsException("User with email=" + patchEmail + " already exists.");
                    });
        }
        UserMapper.patchFromDto(user, patchDto);
        log.debug("UserServiceImpl: patch user " + user + ".");
        return UserMapper.toUserDto(user);
    }

    @Override
    public void remove(Long id) {
        if (!userStorage.remove(id)) {
            throw new NotFoundException("User with id=" + id + " not found.");
        }
        log.debug("UserServiceImpl: remove user id=" + id + ".");
    }
}
