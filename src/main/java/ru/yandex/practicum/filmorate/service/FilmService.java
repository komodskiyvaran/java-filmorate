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
import java.util.Collection;
import java.util.Set;
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
        for (Film film : films) {
            loadGenres(film);
        }
        return films;
    }

    public Film findById(long id) {
        Film film = filmStorage.findById(id);
        loadGenres(film);
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
        filmStorage.delete(id);
    }

    public void addLike(long filmId, long userId) {
        userStorage.findById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        userStorage.findById(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Count must be positive");
        }
        return filmStorage.getPopularFilms(count);
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
                    .orElseThrow(() -> new NotFoundException("Mpa not found"));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                genreStorage.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Genre not found"));
            }
        }
    }

    private void loadGenres(Film film) {
        if (film.getId() == null) return;
        film.setGenres(genreStorage.getFilmGenres(film.getId()));
    }

    private void saveGenres(Film film) {
        if (film.getId() == null) return;
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            genreStorage.deleteFilmGenres(film.getId());
            return;
        }

        Set<Integer> uniqueGenreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        genreStorage.deleteFilmGenres(film.getId());
        for (Integer genreId : uniqueGenreIds) {
            genreStorage.addFilmGenre(film.getId(), genreId);
        }
    }
}