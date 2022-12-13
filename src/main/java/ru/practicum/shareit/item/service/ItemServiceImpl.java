package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.OwnershipException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto add(ItemDto itemDto, Long userId) {
        User owner = getOwner(userId);
        Item item = ItemMapper.fromItemDto(itemDto);
        item.setOwner(owner);
        item = itemStorage.add(item);
        log.debug("ItemServiceImpl: add item " + item + ".");
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAll(Long userId) {
        return itemStorage.getAll(getOwner(userId)).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getById(Long id) {
        return ItemMapper.toItemDto(
                itemStorage.getById(id)
                        .orElseThrow(
                                () -> new NotFoundException("Item with id=" + id + " not found.")
                        )
        );
    }

    @Override
    public ItemDto patch(Long itemId, ItemDto itemDto, Long userId) {
        Item item = itemStorage.getById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id=" + itemId + " not found."));
        if (!item.getOwner().getId().equals(userId)) {
            throw new OwnershipException("User id=" + userId + " is not owner of item id=" + itemId + ".");
        }
        ItemMapper.patchFromDto(item, itemDto);
        log.debug("ItemServiceImpl: patch item " + item + ".");
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemStorage.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private User getOwner(Long userId) {
        return userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));
    }
}
