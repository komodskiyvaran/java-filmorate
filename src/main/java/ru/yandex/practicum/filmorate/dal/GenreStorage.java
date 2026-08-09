package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    List<Genre> findAll();

    Optional<Genre> findById(int id);

    List<Genre> getFilmGenres(Long filmId);

    void addFilmGenre(Long filmId, Integer genreId);

    void deleteFilmGenres(Long filmId);
}