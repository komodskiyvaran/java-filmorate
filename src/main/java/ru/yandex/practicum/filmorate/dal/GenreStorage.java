package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GenreStorage {
    List<Genre> findAll();

    Optional<Genre> findById(int id);

    List<Genre> findAllByIds(Collection<Integer> ids);

    Map<Long, List<Genre>> getGenresByFilmIds(Collection<Long> filmIds);

    void addFilmGenres(Long filmId, Collection<Integer> genreIds);

    void deleteFilmGenres(Long filmId);
}