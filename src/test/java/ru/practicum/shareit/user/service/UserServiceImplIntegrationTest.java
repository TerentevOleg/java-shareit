package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserPatchDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserBuildersTestUtil;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImplIntegrationTest {
    private final UserService userService;
    private final EntityManager entityManager;

    @Test
    void getAll() {
        List<User> users = List.of(
                UserBuildersTestUtil.all(null, "name1", "user1@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name2", "user2@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name3", "user3@mail.ru").userBuild()
        );

        users.forEach(entityManager::persist);

        List<UserDto> userDto = userService.getAll();

        assertThat(userDto).hasSize(3);

        List<UserDto> responseDto = List.of(
                new UserDto(users.get(0).getId(), "name1", "user1@mail.ru"),
                new UserDto(users.get(1).getId(), "name2", "user2@mail.ru"),
                new UserDto(users.get(2).getId(), "name3", "user3@mail.ru")
        );

        assertThat(userDto).containsAll(responseDto);
    }

    @Test
    void getById() {
        List<User> users = List.of(
                UserBuildersTestUtil.all(null, "name1", "user1@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name2", "user2@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name3", "user3@mail.ru").userBuild()
        );

        users.forEach(entityManager::persist);

        Long id = users.get(1).getId();

        UserDto dto = userService.getById(id);

        assertThat(dto).isEqualTo(new UserDto(id, "name2", "user2@mail.ru"));
    }

    @Test
    void add() {
        UserDto userDto = new UserDto(null, "name", "user@mail.ru");

        userService.add(userDto);

        List<User> users = entityManager.createQuery("SELECT u FROM User u", User.class)
                .getResultList();

        assertThat(users.size()).isEqualTo(1);
        User user = users.get(0);
        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo("name");
        assertThat(user.getEmail()).isEqualTo("user@mail.ru");
    }

    @Test
    void patch() {
        UserPatchDto patchDto = new UserPatchDto("patchedName", "patchedUser@mail.ru");

        User user = UserBuildersTestUtil.all(null, "name", "user@mail.ru").userBuild();

        entityManager.persist(user);

        userService.patch(user.getId(), patchDto);

        List<User> users = entityManager.createQuery("SELECT u FROM User u", User.class)
                .getResultList();

        assertThat(users.size()).isEqualTo(1);
        User patchedUser = users.get(0);
        assertThat(patchedUser.getId()).isEqualTo(user.getId());
        assertThat(patchedUser.getName()).isEqualTo(patchDto.getName());
        assertThat(patchedUser.getEmail()).isEqualTo(patchDto.getEmail());
    }

    @Test
    void delete() {
        User user = UserBuildersTestUtil.defaultBuilder().id(null).userBuild();
        entityManager.persist(user);

        userService.delete(user.getId());

        List<User> users = entityManager.createQuery("SELECT u FROM User u", User.class)
                .getResultList();

        assertThat(users.isEmpty()).isTrue();
    }
}
