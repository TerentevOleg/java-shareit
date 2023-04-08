package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {

    BookingDtoResponse getBookingById(long bookingId, long userId);

    List<BookingDtoResponse> getAllBookingsByBooker(long bookerId, BookingState state, long from, int size);

    List<BookingDtoResponse> getAllBookingsByOwner(long ownerId, BookingState state, long from, int size);

    BookingDtoResponse add(BookingDtoRequest dto, long userId);

    BookingDtoResponse bookingStatus(long bookingId, long userId, boolean approved);
}