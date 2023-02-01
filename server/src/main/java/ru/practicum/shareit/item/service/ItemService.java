package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {


    ItemDtoResponse getById(long id, long userId);

    List<ItemDtoResponse> getAll(long userId, long from, int size);

    ItemDto add(ItemDto dto, long userId);

    CommentDtoResponse addComment(CommentDtoRequest commentDtoRequest, long itemId, long userId);

    ItemDto patch(long itemId, ItemPatchDto dto, long userId);

    List<ItemDto> search(String text, long from, int size);

}