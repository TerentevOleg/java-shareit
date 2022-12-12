package ru.practicum.shareit.util;

import lombok.Getter;

import java.util.*;

public abstract class CrudStorageImpl<T> implements CrudStorage<T> {

    @Getter
    public final Map<Long, T> entities = new HashMap<>();
    private long nextId = 1;

    @Override
    public T add(T entity) {
        long id = nextId++;
        entity = setEntityId(entity, id);
        entities.put(id, entity);
        return entity;
    }

    @Override
    public boolean remove(Long id) {
        return Objects.nonNull(
                entities.remove(id));
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<>(entities.values());
    }

    @Override
    public Optional<T> getById(Long id) {
        return Optional.ofNullable(entities.get(id));
    }

    protected abstract T setEntityId(T entity, Long id);

    protected abstract Long getEntityId(T entity);
}
