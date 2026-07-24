package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

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
        Film oldFilm = filmStorage.findById(updatedFilm.getId());

        oldFilm.setName(updatedFilm.getName());
        oldFilm.setDescription(updatedFilm.getDescription());
        oldFilm.setReleaseDate(updatedFilm.getReleaseDate());
        oldFilm.setDuration(updatedFilm.getDuration());

        return filmStorage.update(oldFilm);
    }

    public void delete(long id) {
        filmStorage.delete(id);
    }

    public void addLike(long id, long userId) {
        userStorage.findById(userId);
        filmStorage.addLike(id, userId);
    }

    public void removeLike(long id, long userId) {
        userStorage.findById(userId);
        filmStorage.removeLike(id, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }
}
