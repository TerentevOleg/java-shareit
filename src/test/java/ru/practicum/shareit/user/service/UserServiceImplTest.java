package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserPatchDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.mapper.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Spy
    private static UserMapper userMapper = new UserMapperImpl();

    @NoArgsConstructor(staticName = "defaultBuilder")
    @AllArgsConstructor(staticName = "all")
    @Setter
    @Accessors(chain = true, fluent = true)
    private static class UserBuilderTest {
        private Long id = 1L;
        private String name = "name";
        private String email = "user@mail.ru";

        public User userBuild() {
            User user = new User();
            user.setId(id);
            user.setName(name);
            user.setEmail(email);
            return user;
        }

        public UserDto buildDto() {
            return new UserDto(id, name, email);
        }

        public UserPatchDto buildPatchDto() {
            return new UserPatchDto(name, email);
        }
    }

    @Test
    void givenPatchWithAbsentUser_whenPatch_thenThrowNotFoundException() {
        long id = 1L;
        Mockito.when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.patch(id, UserBuilderTest.defaultBuilder().buildPatchDto()));

        Mockito.verify(userRepository).findById(id);
    }

    @Test
    void givenPatchWithName_whenPatch_thenReturnPatchedDto() {
        UserBuilderTest userBuilder = UserBuilderTest.defaultBuilder();
        User initialUser = userBuilder.userBuild();
        long id = initialUser.getId();
        String patchName = initialUser.getName() + " updated";
        UserPatchDto requestDto = new UserPatchDto(patchName, null);
        userBuilder.name(patchName);
        UserDto patchedDto = userBuilder.buildDto();

        Mockito.when(userRepository.findById(id))
                .thenReturn(Optional.of(initialUser));

        assertEquals(patchedDto, userService.patch(id, requestDto));
    }

    @Test
    void givenPatchWithEmail_whenPatch_thenReturnPatchedDto() {
        UserBuilderTest userBuilder = UserBuilderTest.defaultBuilder();
        User initialEntity = userBuilder.userBuild();
        long id = initialEntity.getId();
        String patchEmail = initialEntity.getEmail() + " updated";
        UserPatchDto requestDto = new UserPatchDto(null, patchEmail);
        userBuilder.email(patchEmail);
        UserDto patchedDto = userBuilder.buildDto();

        Mockito.when(userRepository.findById(id))
                .thenReturn(Optional.of(initialEntity));

        assertEquals(patchedDto, userService.patch(id, requestDto));
    }

    @Test
    void givenRemoveWithCorrectId_whenRemove_thenNotThrowException() {
        long id = 1;
        assertDoesNotThrow(() -> userService.delete(id));
        Mockito.verify(userRepository).deleteById(id);
    }

    @Test
    void givenRemoveWithAbsentId_whenRemove_thenThrowNotFoundException() {
        long id = 1;
        Mockito.doThrow(EmptyResultDataAccessException.class)
                .when(userRepository).deleteById(id);

        assertThrows(NotFoundException.class, () -> userService.delete(id));

        Mockito.verify(userRepository).deleteById(id);
    }

    @Test
    void givenGetAllWithNoUsers_whenGetAll_thenReturnEmptyList() {
        Mockito.when(userRepository.findAll())
                .thenReturn(Collections.emptyList());

        assertEquals(Collections.emptyList(), userService.getAll());

        Mockito.verify(userRepository).findAll();
    }

    @Test
    void givenGetAllWithExistingUsers_whenGetAll_ThenReturnListOfDto() {
        UserBuilderTest userBuilder = UserBuilderTest.all(1L, "name1", "user1@mail.ru");
        User user1 = userBuilder.userBuild();
        UserDto dto1 = userBuilder.buildDto();
        userBuilder.id(2L).name("name2").email("user2@mail.ru");
        User user2 = userBuilder.userBuild();
        UserDto dto2 = userBuilder.buildDto();
        List<User> users = List.of(user1, user2);
        List<UserDto> dto = List.of(dto1, dto2);

        Mockito.when(userRepository.findAll()).thenReturn(users);

        assertEquals(dto, userService.getAll());
    }

    @Test
    void givenGetByIdWithAbsentId_whenGetById_thenThrowNotFoundException() {
        long id = 1L;
        Mockito.when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(id));
    }

    @Test
    void givenGetByIdWithExistingId_whenGetById_thenReturnDto() {
        UserBuilderTest userBuilder = UserBuilderTest.defaultBuilder();
        User user = userBuilder.userBuild();
        UserDto dto = userBuilder.buildDto();
        long id = user.getId();

        Mockito.when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        assertEquals(dto, userService.getById(id));
    }
}