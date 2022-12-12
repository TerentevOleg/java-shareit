package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto add(UserDto dto);

    List<UserDto> getAll();

    UserDto getById(Long id);

    UserDto patch(Long id, UserDto patchDto);

    void remove(Long id);
}
