package ru.practicum.shareit.booking.dto;

import lombok.Value;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Value
public class BookingDtoRequest {

    @FutureOrPresent
    @NotNull
    LocalDateTime start;

    @Future
    @NotNull
    LocalDateTime end;

    @NotNull
    Long itemId;
}