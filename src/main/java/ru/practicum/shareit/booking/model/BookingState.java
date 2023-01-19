package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.exception.CustomValidationException;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState converter(String name) {
        try {
            return BookingState.valueOf(name);
        } catch (IllegalArgumentException exception) {
            throw new CustomValidationException("Unknown state: " + name);
        }
    }
}
