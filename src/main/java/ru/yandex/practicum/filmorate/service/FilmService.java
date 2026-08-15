package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.dal.MpaStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;

@Slf4j
@Service
public class FilmService {

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage,
                       GenreStorage genreStorage, MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        attachGenres(films);
        return films;
    }

    public Film findById(long id) {
        Film film = filmStorage.findById(id);
        attachGenres(List.of(film));
        return film;
    }

    public Film create(Film film) {
        validateFilm(film);
        Film created = filmStorage.create(film);
        saveGenres(created);
        return created;
    }

    public Film update(Film updatedFilm) {
        if (updatedFilm.getId() == null) {
            throw new ValidationException(ID_MUST_BE_SPECIFIED);
        }
        filmStorage.findById(updatedFilm.getId());
        validateFilm(updatedFilm);
        Film updated = filmStorage.update(updatedFilm);
        saveGenres(updated);
        return updated;
    }

    public void delete(long id) {
        genreStorage.deleteFilmGenres(id);
        filmStorage.delete(id);
    }

    public void addLike(long filmId, long userId) {
        filmStorage.findById(filmId);
        userStorage.findById(userId);

        if (filmStorage.hasLike(filmId, userId)) {
            log.warn(FILM_LIKE_ALREADY_EXISTS);
            return;
        }
        filmStorage.addLike(filmId, userId);
        log.info("User {} liked film {}", userId, filmId);
    }

    public void removeLike(long filmId, long userId) {
        filmStorage.findById(filmId);
        userStorage.findById(userId);

        if (!filmStorage.hasLike(filmId, userId)) {
            log.warn(FILM_LIKE_NOT_FOUND);
            return;
        }
        filmStorage.removeLike(filmId, userId);
        log.info("User {} unliked film {}", userId, filmId);
    }

    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException(FILM_POPULAR_COUNT_NOT_POSITIVE);
        }
        Collection<Film> films = filmStorage.getPopularFilms(count);
        attachGenres(films);
        return films;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException(FILM_NAME_EMPTY);
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException(FILM_DESCRIPTION_TOO_LONG);
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            throw new ValidationException(FILM_RELEASE_DATE_TOO_EARLY);
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException(FILM_DURATION_NOT_POSITIVE);
        }

        if (film.getMpaId() != null) {
            mpaStorage.findById(film.getMpaId().intValue())
                    .orElseThrow(() -> new NotFoundException(FILM_MPA_NOT_FOUND + film.getMpaId()));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> requestedIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            Set<Integer> foundIds = genreStorage.findAllByIds(requestedIds).stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            requestedIds.removeAll(foundIds);
            if (!requestedIds.isEmpty()) {
                throw new NotFoundException(FILM_GENRE_NOT_FOUND + requestedIds);
            }
        }
    }

    private void attachGenres(Collection<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());
        Map<Long, List<Genre>> genresByFilmId = genreStorage.getGenresByFilmIds(filmIds);
        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), new ArrayList<>()));
        }
    }

    private void saveGenres(Film film) {
        if (film.getId() == null) {
            return;
        }
        genreStorage.deleteFilmGenres(film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        Set<Integer> uniqueGenreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        genreStorage.addFilmGenres(film.getId(), uniqueGenreIds);
    }
}