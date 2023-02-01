package ru.practicum.shareit.item.dto;

import lombok.Value;

@Value
public class ItemDto {
    Long id;

    String name;

    String description;

    Boolean available;

    Long requestId;
}