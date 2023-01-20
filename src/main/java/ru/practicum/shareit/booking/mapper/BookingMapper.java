package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {UserMapper.class, ItemMapper.class})
public interface BookingMapper {

    BookingDtoResponse toDto(Booking booking);

    List<BookingDtoResponse> toDto(List<Booking> bookings);

    @Mapping(target = "status", constant = "WAITING")
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booker", ignore = true)
    Booking fromDto(BookingDtoRequest dto);
}