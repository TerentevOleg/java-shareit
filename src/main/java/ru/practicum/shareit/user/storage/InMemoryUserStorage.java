package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.CrudStorageImpl;

import java.util.Objects;
import java.util.Optional;

@Repository
public class InMemoryUserStorage extends CrudStorageImpl<User> implements UserStorage {

    @Override
    public Optional<User> getByEmail(String email) {
        return super.getEntities().values().stream()
                .filter(user -> Objects.equals(user.getEmail(), email))
                .findAny();
    }

    @Override
    public Optional<User> getByEmailWithoutId(String email, Long id) {
        return super.getEntities().values().stream()
                .filter(user ->
                        Objects.equals(user.getEmail(), email) && !Objects.equals(user.getId(), id))
                .findAny();
    }

    @Override
    protected Long getEntityId(User entity) {
        return entity.getId();
    }

    @Override
    protected User setEntityId(User entity, Long id) {
        entity.setId(id);
        return entity;
    }
}
