package ru.practicum.shareit.util;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = NullableNotBlankValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface NullableNotBlank {
    String message() default "Field can't be blank.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}