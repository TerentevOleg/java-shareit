package ru.practicum.shareit.booking.dto;

import lombok.Value;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Value
public class BookingDtoShort {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    Long bookerId;
    BookingStatus status;
}