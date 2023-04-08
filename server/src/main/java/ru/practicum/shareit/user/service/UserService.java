package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserPatchDto;

import java.util.List;

public interface UserService {

    UserDto getById(long id);

    List<UserDto> getAll();

    UserDto add(UserDto dto);

    UserDto patch(long id, UserPatchDto patchDto);

    void delete(long id);
}