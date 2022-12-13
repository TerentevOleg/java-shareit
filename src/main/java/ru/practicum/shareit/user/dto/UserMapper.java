package ru.practicum.shareit.user.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User fromUserDto(UserDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return user;
    }

    public static void patchFromDto(User userToPatch, UserDto patchDto) {
        String patchEmail = patchDto.getEmail();
        if (Objects.nonNull(patchEmail)) {
            userToPatch.setEmail(patchEmail);
        }
        String patchName = patchDto.getName();
        if (Objects.nonNull(patchName)) {
            userToPatch.setName(patchName);
        }
    }
}
