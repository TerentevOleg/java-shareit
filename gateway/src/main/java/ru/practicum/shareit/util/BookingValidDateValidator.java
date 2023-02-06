package ru.practicum.shareit.util;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.util.Objects;

public class BookingValidDateValidator implements ConstraintValidator<BookingValidDate, BookingDtoRequest> {
    @Override
    public void initialize(BookingValidDate constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(BookingDtoRequest bookingDtoRequest, ConstraintValidatorContext constraintValidatorContext) {
        LocalDateTime start = bookingDtoRequest.getStart();
        LocalDateTime end = bookingDtoRequest.getEnd();
        if (Objects.isNull(start) || Objects.isNull(end)) {
            return false;
        }
        return start.isBefore(end);
    }
}
