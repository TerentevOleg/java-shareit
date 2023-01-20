package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.*;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto getById(long id) {
        return userMapper.toDto(
                userRepository.findById(id)
                        .orElseThrow(
                                () -> new NotFoundException("UserServiceImpl: User with id=" + id + " not found.")
                        )
        );
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserDto add(UserDto userDto) {
        User user = userRepository.save(userMapper.fromDto(userDto));
        log.debug("UserServiceImpl: add user " + user + ".");
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserDto patch(long id, UserPatchDto patchDto) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("UserServiceImpl: user with id=" + id + " not found.")
                );
        userMapper.updateWithPatchDto(user, patchDto);
        log.debug("UserServiceImpl: patch user " + user + ".");
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(long id) {
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            log.debug(e.getMessage(), e);
            throw new NotFoundException("UserServiceImpl: user with id=" + id + " not found.");
        }
        log.debug("UserServiceImpl: delete user id=" + id + ".");
    }
}