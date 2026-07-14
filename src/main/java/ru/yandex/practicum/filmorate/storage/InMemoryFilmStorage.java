package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;
import static ru.yandex.practicum.filmorate.exception.ErrorMessages.ID_MUST_BE_SPECIFIED;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }
    @Override
    public Film findById(long id) {
        return films.values().stream()
                .filter(user -> user.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(FILM_NOT_FOUND + id));
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Film created successfully: {} (id={})", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film update(Film updatedFilm) {
        log.debug("Request to update film: {}", updatedFilm);

        if (updatedFilm.getId() == null) {
            log.warn("Update failed: " + ID_MUST_BE_SPECIFIED);
            throw new ValidationException(ID_MUST_BE_SPECIFIED);
        }

        Film film = films.get(updatedFilm.getId());
        if (film == null) {
            String message = FILM_NOT_FOUND + updatedFilm.getId();

            log.warn(message);
            throw new NotFoundException(message);
        }

        updateFieldFilm(film, updatedFilm);

        log.info("Film updated successfully: {} (id={})", film.getName(), film.getId());
        return film;
    }

    @Override
    public void delete(long id) {
        findById(id);
        log.info("The film with id = {} has been successfully deleted", id);
        films.remove(id);
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
