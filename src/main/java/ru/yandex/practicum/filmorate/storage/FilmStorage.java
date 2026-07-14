package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();
    Film create(Film film);
    Film findById(long id);
    Film update(Film film);
    void delete(long id);
}
