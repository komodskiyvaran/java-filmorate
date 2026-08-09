package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;

@Slf4j
@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL = "SELECT f.*, r.rating_id, r.name AS rating_name FROM films f " +
            "LEFT JOIN rating r ON f.rating_id = r.rating_id";

    private static final String FIND_BY_ID = "SELECT f.*, r.rating_id, r.name AS rating_name FROM films f " +
            "LEFT JOIN rating r ON f.rating_id = r.rating_id WHERE f.film_id = ?";

    private static final String INSERT = "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String DELETE = "DELETE FROM films WHERE film_id = ?";
    private static final String ADD_LIKE = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String DELETE_LIKES_BY_FILM =
            "DELETE FROM film_likes WHERE film_id = ?";
    private static final String CHECK_LIKE = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_POPULAR = "SELECT f.*, r.rating_id, r.name AS rating_name FROM films f " +
            "LEFT JOIN rating r ON f.rating_id = r.rating_id " +
            "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
            "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.rating_id, r.name " +
            "ORDER BY COUNT(fl.user_id) DESC LIMIT ?";

    private static final String INSERT_GENRE = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRE = "DELETE FROM film_genre WHERE film_id = ?";

    private static final String LOAD_GENRES = "SELECT g.* FROM genres g " +
            "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
            "WHERE fg.film_id = ?";

    private final GenreRowMapper genreMapper;
    private final MpaRowMapper mpaMapper;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> filmMapper,
                         GenreRowMapper genreMapper, MpaRowMapper mpaMapper) {
        super(jdbc, filmMapper);
        this.genreMapper = genreMapper;
        this.mpaMapper = mpaMapper;
    }

    private Mpa loadMpa(Long mpaId) {
        if (mpaId == null) return null;
        try {
            String sql = "SELECT * FROM rating WHERE rating_id = ?";
            return jdbc.queryForObject(sql, mpaMapper, mpaId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private List<Genre> loadGenres(Long filmId) {
        if (filmId == null) return new ArrayList<>();
        try {
            return jdbc.query(LOAD_GENRES, genreMapper, filmId);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    private void saveGenres(Long filmId, List<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        List<Integer> uniqueGenreIds = genres.stream()
                .map(Genre::getId)
                .distinct()
                .collect(Collectors.toList());

        for (Integer genreId : uniqueGenreIds) {
            jdbc.update(INSERT_GENRE, filmId, genreId.longValue());
        }
    }

    private void deleteGenres(Long filmId) {
        jdbc.update(DELETE_GENRE, filmId);
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(FIND_ALL, mapper);
        for (Film film : films) {
            film.setMpa(loadMpa(film.getMpaId()));
            film.setGenres(loadGenres(film.getId()));  // ← теперь List
        }
        return films;
    }

    @Override
    public Film findById(long id) {
        Film film = findOne(FIND_BY_ID, id)
                .orElseThrow(() -> new NotFoundException(FILM_NOT_FOUND + id));
        film.setMpa(loadMpa(film.getMpaId()));
        film.setGenres(loadGenres(film.getId()));  // ← теперь List
        return film;
    }

    @Override
    public Film create(Film film) {
        long id = insert(INSERT,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaId()
        );
        film.setId(id);
        saveGenres(id, film.getGenres());
        return film;
    }

    @Override
    public Film update(Film film) {
        update(UPDATE,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaId(),
                film.getId()
        );
        deleteGenres(film.getId());
        saveGenres(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public void delete(long id) {
        findById(id);

        jdbc.update(DELETE_LIKES_BY_FILM, id);
        deleteGenres(id);
        delete(DELETE, id);
    }

    @Override
    public void addLike(long filmId, long userId) {
        Integer count = jdbc.queryForObject(CHECK_LIKE, Integer.class, filmId, userId);
        if (count > 0) {
            log.warn(FILM_LIKE_ALREADY_EXISTS);
            return;
        }
        jdbc.update(ADD_LIKE, filmId, userId);
        log.info("User {} liked film {}", userId, filmId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        int deleted = jdbc.update(REMOVE_LIKE, filmId, userId);
        if (deleted == 0) {
            log.warn(FILM_LIKE_NOT_FOUND);
        } else {
            log.info("User {} unliked film {}", userId, filmId);
        }
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }

        List<Film> films = jdbc.query(GET_POPULAR, mapper, count);

        for (Film film : films) {
            film.setMpa(loadMpa(film.getMpaId()));
            film.setGenres(loadGenres(film.getId()));
        }

        return films;
    }
}