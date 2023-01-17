package ru.practicum.shareit.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class NullableNotBlankValidator implements ConstraintValidator<NullableNotBlank, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return Objects.isNull(s) || !s.isBlank();
    }
}