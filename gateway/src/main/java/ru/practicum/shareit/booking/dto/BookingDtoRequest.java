package ru.practicum.shareit.booking.dto;

import lombok.Value;
import ru.practicum.shareit.util.BookingValidDate;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Value
@BookingValidDate
public class BookingDtoRequest {

    @FutureOrPresent
    LocalDateTime start;

    @Future
    LocalDateTime end;

    @NotNull
    Long itemId;
}