package ru.yandex.practicum.filmorate.exception;

public class ErrorMessages {
    private ErrorMessages() {
    }

    public static final String FILM_NAME_EMPTY = "Film name cannot be empty.";
    public static final String FILM_DESCRIPTION_TOO_LONG = "Description length cannot exceed 200 characters.";
    public static final String FILM_RELEASE_DATE_TOO_EARLY = "Release date must be after 28 December 1895.";
    public static final String FILM_DURATION_NOT_POSITIVE = "Duration must be positive.";

    public static final String USER_EMAIL_EMPTY = "Email must be specified.";
    public static final String USER_EMAIL_INVALID = "Email must contain '@'.";
    public static final String USER_LOGIN_EMPTY = "Login must be specified.";
    public static final String USER_BIRTHDAY_IN_FUTURE = "The birthday shouldn't be in the future";

    public static final String ID_MUST_BE_SPECIFIED = "Id must be specified.";
    public static final String FILM_NOT_FOUND = "Film with id %d not found.";
    public static final String USER_NOT_FOUND = "User with id %d not found.";
}
