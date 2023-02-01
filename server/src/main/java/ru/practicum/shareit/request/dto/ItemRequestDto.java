package ru.practicum.shareit.request.dto;

import lombok.Value;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Value
public class ItemRequestDto {

    Long id;

    String description;

    LocalDateTime created;

    List<ItemDto> items;
}

