package ru.practicum.shareit.item.mapper;

import org.mapstruct.*;
import ru.practicum.shareit.booking.mapper.BookingShortMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemPatchDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {BookingShortMapper.class, CommentMapper.class})
public interface ItemMapper {

    ItemDto toDto(Item item);

    Item fromDto(ItemDto itemDto);

    @Mapping(target = "id", source = "item.id")
    ItemDtoResponse toDtoResponse(Item item, List<Comment> comments, Booking lastBooking, Booking nextBooking);

    @Mapping(target = "nextBooking", ignore = true)
    @Mapping(target = "lastBooking", ignore = true)
    ItemDtoResponse toDtoResponse(Item item, List<Comment> comments);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateWithPatchDto(@MappingTarget Item item, ItemPatchDto itemPatchDto);
}