package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

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

        if (!films.containsKey(updatedFilm.getId())) {
            String message = FILM_NOT_FOUND + updatedFilm.getId();

            log.warn(message);
            throw new NotFoundException(message);
        }

        log.info("Film updated successfully: {} (id={})", updatedFilm.getName(), updatedFilm.getId());
        films.put(updatedFilm.getId(), updatedFilm);
        return updatedFilm;
    }

    @Override
    public void delete(long id) {
        findById(id);
        log.info("The film with id = {} has been successfully deleted", id);
        films.remove(id);
    }

    @Override
    public void addLike(long id, long userId) {
        Film film = findById(id);

        if (film.getLikes().contains(userId)) {
            log.warn(FILM_LIKE_ALREADY_EXISTS);
            return;
        }

        log.info("A user with ID {} liked a movie with ID {}", id, userId);
        film.getLikes().add(userId);
    }

    @Override
    public void removeLike(long id, long userId) {
        Film film = findById(id);

        if (!film.getLikes().contains(userId)) {
            log.warn(FILM_LIKE_NOT_FOUND);
            return;
        }
        log.info("A user with ID {} unliked a movie with ID {}", id, userId);
        film.getLikes().remove(userId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        return findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private long getNextId() {
        return ++currentId;
    }
}
