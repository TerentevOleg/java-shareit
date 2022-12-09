package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemServiceImpl implements ItemService{

    @Override
    public ItemDto add(ItemDto itemDto, Long userId) {
        return null;
    }

    @Override
    public ItemDto patch(Long itemId, ItemDto itemDto, Long userId) {
        return null;
    }

    @Override
    public ItemDto getById(Long id) {
        return null;
    }

    @Override
    public List<ItemDto> getAll(Long userId) {
        return null;
    }

    @Override
    public List<ItemDto> search(String text) {
        return null;
    }
}
