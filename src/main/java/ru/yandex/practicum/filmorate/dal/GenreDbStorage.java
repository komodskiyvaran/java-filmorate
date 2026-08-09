package ru.yandex.practicum.filmorate.dal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    private static final String FIND_ALL = "SELECT * FROM genres ORDER BY genre_id";
    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String GET_FILM_GENRES = "SELECT g.* FROM genres g " +
            "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
            "WHERE fg.film_id = ?";
    private static final String ADD_FILM_GENRE = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genre WHERE film_id = ?";

    @Override
    public List<Genre> findAll() {
        return jdbc.query(FIND_ALL, mapper);
    }

    @Override
    public Optional<Genre> findById(int id) {
        try {
            Genre genre = jdbc.queryForObject(FIND_BY_ID, mapper, id);
            return Optional.ofNullable(genre);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Genre> getFilmGenres(Long filmId) {
        return jdbc.query(GET_FILM_GENRES, mapper, filmId);
    }

    @Override
    public void addFilmGenre(Long filmId, Integer genreId) {
        jdbc.update(ADD_FILM_GENRE, filmId, genreId);
    }

    @Override
    public void deleteFilmGenres(Long filmId) {
        jdbc.update(DELETE_FILM_GENRES, filmId);
    }
}