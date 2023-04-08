package ru.practicum.shareit.user.dto;

import lombok.Value;

@Value
public class UserPatchDto {

    String name;

    String email;
}