package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;


import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private final Map<Long, Film> films = new HashMap<>();


    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    // пост запрос
    @PostMapping
    public Film create(@RequestBody Film film) {
        log.debug("Request to create film: {}", film);

        checkFilm(film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Film created successfully: {} (id={})", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film updatedFilm) {
        log.debug("Request to update film: {}", updatedFilm);

        if (updatedFilm.getId() == null) {
            log.warn("Update failed: " + ID_MUST_BE_SPECIFIED);
            throw new ValidationException(ID_MUST_BE_SPECIFIED);
        }

        Film film = films.get(updatedFilm.getId());
        if (film == null) {
            String message = String.format(FILM_NOT_FOUND, updatedFilm.getId());

            log.warn(message);
            throw new NotFoundException(message);
        }

        checkFilm(updatedFilm);

        film.setName(updatedFilm.getName());
        film.setDescription(updatedFilm.getDescription());
        film.setReleaseDate(updatedFilm.getReleaseDate());
        film.setDuration(updatedFilm.getDuration());

        log.info("Film updated successfully: {} (id={})", film.getName(), film.getId());
        return film;
    }


    private long getNextId() {
        return films.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
    }

    void checkFilm(Film film) {
        if (film.getName() == null || film.getName().trim().isEmpty()) {
            log.warn(FILM_NAME_EMPTY);
            throw new ValidationException(FILM_NAME_EMPTY);
        }
        if (film.getDescription() != null &&
                film.getDescription().trim().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn(FILM_DESCRIPTION_TOO_LONG);
            throw new ValidationException(FILM_DESCRIPTION_TOO_LONG);
        }

        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.warn(FILM_RELEASE_DATE_TOO_EARLY);
            throw new ValidationException(FILM_RELEASE_DATE_TOO_EARLY);
        }

        if (film.getDuration() != null && film.getDuration() <= 0) {
            log.warn(FILM_DURATION_NOT_POSITIVE);
            throw new ValidationException(FILM_DURATION_NOT_POSITIVE);
        }
    }
}
