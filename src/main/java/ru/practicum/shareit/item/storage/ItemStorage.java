package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.CrudStorage;

import java.util.List;

public interface ItemStorage extends CrudStorage<Item> {

    List<Item> getAll(User owner);

    List<Item> search(String text);
}
