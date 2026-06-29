package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;



    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Film created successfully: {} (id={})", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film updatedFilm) {
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

        updateFieldFilm(film, updatedFilm);

        log.info("Film updated successfully: {} (id={})", film.getName(), film.getId());
        return film;
    }

    private void updateFieldFilm(Film film, Film updatedFilm) {
        film.setName(updatedFilm.getName());
        film.setDescription(updatedFilm.getDescription());
        film.setReleaseDate(updatedFilm.getReleaseDate());
        film.setDuration(updatedFilm.getDuration());
    }

    private long getNextId() {
        return ++currentId;
    }
}
