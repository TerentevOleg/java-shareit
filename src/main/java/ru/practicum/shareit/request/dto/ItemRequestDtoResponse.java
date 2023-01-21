package ru.practicum.shareit.request.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemRequestDtoResponse {

    Long id;

    String description;

    LocalDateTime created;
}
