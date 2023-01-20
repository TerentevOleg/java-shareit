package ru.practicum.shareit.item.dto;

import lombok.Value;
import ru.practicum.shareit.booking.dto.BookingDtoShort;

import java.util.List;

@Value
public class ItemDtoResponse {
    Long id;
    String name;
    String description;
    Boolean available;
    BookingDtoShort lastBooking;
    BookingDtoShort nextBooking;
    List<CommentDtoResponse> comments;
}