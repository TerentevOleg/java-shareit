package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.CrudStorageImpl;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemStorage extends CrudStorageImpl<Item> implements ItemStorage {

    private final Map<Long, List<Item>> userItemIndex = new HashMap<>();

    @Override
    public Item add(Item item) {
        item = super.add(item);
        userItemIndex
                .computeIfAbsent(item.getOwner().getId(), ownerId -> new LinkedList<>())
                .add(item);
        return item;
    }

    @Override
    public List<Item> getAll(User owner) {
        return userItemIndex.getOrDefault(owner.getId(), Collections.emptyList());
    }

    @Override
    public boolean remove(Long id) {
        userItemIndex.values()
                .forEach(items -> items.removeIf(item -> item.getId().equals(id)));
        return super.remove(id);
    }

    @Override
    protected Item setEntityId(Item entity, Long id) {
        entity.setId(id);
        return entity;
    }

    @Override
    protected Long getEntityId(Item entity) {
        return entity.getId();
    }

    @Override
    public List<Item> search(String text) {
        String textSearch = text.toLowerCase();
        return getEntities().values().stream()
                .filter(item ->
                        (item.getName().toLowerCase().contains(textSearch)
                                || item.getDescription().toLowerCase().contains(textSearch))
                                && item.getAvailable())
                .collect(Collectors.toList());
    }
}
