package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film findById(long id);

    Film update(Film film);

    void delete(long id);

    void addLike(long id, long userId);

    void removeLike(long id, long userId);

    Collection<Film> getPopularFilms(int count);
}
