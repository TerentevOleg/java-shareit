package ru.practicum.shareit.request.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ItemRequestDtoResponse {

    Long id;

    String description;

    LocalDateTime created;
}
