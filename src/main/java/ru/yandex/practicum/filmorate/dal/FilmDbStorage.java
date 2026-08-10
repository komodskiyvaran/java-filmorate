package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Collections;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.FILM_NOT_FOUND;

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
    private static final String DELETE_LIKES_BY_FILM = "DELETE FROM film_likes WHERE film_id = ?";
    private static final String CHECK_LIKE = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";

    private static final String GET_POPULAR = "SELECT f.*, r.rating_id, r.name AS rating_name FROM films f " +
            "LEFT JOIN rating r ON f.rating_id = r.rating_id " +
            "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
            "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.rating_id, r.name " +
            "ORDER BY COUNT(fl.user_id) DESC LIMIT ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> filmMapper) {
        super(jdbc, filmMapper);
    }

    @Override
    public Collection<Film> findAll() {
        return jdbc.query(FIND_ALL, mapper);
    }

    @Override
    public Film findById(long id) {
        return findOne(FIND_BY_ID, id)
                .orElseThrow(() -> new NotFoundException(FILM_NOT_FOUND + id));
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
        return film;
    }

    @Override
    public void delete(long id) {
        findById(id);
        jdbc.update(DELETE_LIKES_BY_FILM, id);
        delete(DELETE, id);
    }

    @Override
    public void addLike(long filmId, long userId) {
        jdbc.update(ADD_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbc.update(REMOVE_LIKE, filmId, userId);
    }

    @Override
    public boolean hasLike(long filmId, long userId) {
        Integer count = jdbc.queryForObject(CHECK_LIKE, Integer.class, filmId, userId);
        return count != null && count > 0;
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        return jdbc.query(GET_POPULAR, mapper, count);
    }
}