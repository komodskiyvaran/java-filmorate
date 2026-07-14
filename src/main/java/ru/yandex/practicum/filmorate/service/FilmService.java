package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.FILM_LIKE_ALREADY_EXISTS;
import static ru.yandex.practicum.filmorate.exception.ErrorMessages.FILM_LIKE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(long id) {
        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film updatedFilm) {
        return filmStorage.update(updatedFilm);
    }

    public void delete(long id) {
        filmStorage.delete(id);
    }

    public void addLike(long id, long userId) {
        Film film = filmStorage.findById(id);
        userStorage.findById(userId);

        if (film.getLikes().contains(userId)) {
            log.warn(FILM_LIKE_ALREADY_EXISTS);
            return;
        }

        log.info("A user with ID {} liked a movie with ID {}", id, userId);
        film.getLikes().add(userId);
    }

    public void removeLike(long id, long userId) {
        Film film = filmStorage.findById(id);
        userStorage.findById(userId);

        if (!film.getLikes().contains(userId)) {
            log.warn(FILM_LIKE_NOT_FOUND);
            return;
        }
        log.info("A user with ID {} unliked a movie with ID {}", id, userId);
        film.getLikes().remove(userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
