package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface ItemRequestMapper {

    ItemRequestDtoResponse toDto(ItemRequest itemRequest);

    ItemRequestDto toRequestDto(ItemRequest itemRequest, List<Item> items);

    @Named("toRequestDto")
    default List<ItemRequestDto> toRequestDto(List<ItemRequest> itemRequests,
                                              Map<Long, List<Item>> itemsByRequestId) {
        return itemRequests.stream()
                .map(itemRequest -> {
                    List<Item> items = itemsByRequestId.get(itemRequest.getId());
                    return toRequestDto(itemRequest, Objects.isNull(items) ? Collections.emptyList() : items);
                })
                .collect(Collectors.toList());

    }

    ItemRequest fromDto(ItemRequestDtoRequest dto);
}
