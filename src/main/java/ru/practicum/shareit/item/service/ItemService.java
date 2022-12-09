package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto add(ItemDto itemDto, Long userId);

    ItemDto patch(Long itemId, ItemDto itemDto, Long userId);

    ItemDto getById(Long id);

    List<ItemDto> getAll(Long userId);

    List<ItemDto> search(String text);
}
