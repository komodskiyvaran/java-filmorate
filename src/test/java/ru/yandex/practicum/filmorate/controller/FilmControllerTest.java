package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    @Test
    void shouldThrowWhenFilmIsEmpty() {
        Film film = new Film();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.checkFilm(film)
        );

        assertEquals(FILM_NAME_EMPTY, exception.getMessage());
    }

    @Test
    void shouldCreateFilmWithValidData() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        assertDoesNotThrow(() -> controller.checkFilm(film));
    }

    @Test
    void shouldThrowWhenNameIsEmpty() {
        Film film = Film.builder()
                .name("")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.checkFilm(film)
        );

        assertEquals(FILM_NAME_EMPTY, exception.getMessage());
    }

    @Test
    void shouldThrowWhenDescriptionTooLong() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("a".repeat(201))
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.checkFilm(film)
        );

        assertEquals(FILM_DESCRIPTION_TOO_LONG, exception.getMessage());
    }

    @Test
    void shouldThrowWhenReleaseDateTooEarly() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(120L)
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.checkFilm(film)
        );

        assertEquals(FILM_RELEASE_DATE_TOO_EARLY, exception.getMessage());
    }

    @Test
    void shouldThrowWhenDurationIsZero() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0L)
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.checkFilm(film)
        );

        assertEquals(FILM_DURATION_NOT_POSITIVE, exception.getMessage());
    }

    @Test
    void shouldThrowWhenDurationIsNegative() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-10L)
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.checkFilm(film)
        );

        assertEquals(FILM_DURATION_NOT_POSITIVE, exception.getMessage());
    }

    @Test
    void shouldAllowNullDescription() {
        Film film = Film.builder()
                .name("Valid Film")
                .description(null)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        assertDoesNotThrow(() -> controller.checkFilm(film));
    }

    @Test
    void shouldAllowNullReleaseDate() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Description")
                .releaseDate(null)
                .duration(120L)
                .build();

        assertDoesNotThrow(() -> controller.checkFilm(film));
    }
}