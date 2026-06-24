package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.exception.ErrorMessages;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReleaseDateValidator.class)
public @interface ValidReleaseDate {
    String message() default ErrorMessages.FILM_RELEASE_DATE_TOO_EARLY;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}