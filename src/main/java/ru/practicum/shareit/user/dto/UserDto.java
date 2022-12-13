package ru.practicum.shareit.user.dto;

import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class UserDto {

    Long id;

    @NotBlank
    String name;

    @Email
    @NotBlank
    @NotNull
    String email;
}
