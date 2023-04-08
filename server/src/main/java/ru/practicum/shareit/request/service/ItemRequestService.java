package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDtoResponse add(ItemRequestDtoRequest dto, long userId);

    ItemRequestDto findById(long id, long userId);

    List<ItemRequestDto> findByRequester(long requesterId);

    List<ItemRequestDto> findByOtherUsers(long userId, long from, int size);
}
