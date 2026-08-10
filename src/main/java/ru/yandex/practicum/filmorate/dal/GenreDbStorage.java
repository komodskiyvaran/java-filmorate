package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    private static final String FIND_ALL = "SELECT * FROM genres ORDER BY genre_id";
    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE genre_id = ?";
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
    public List<Genre> findAllByIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM genres WHERE genre_id IN (" + placeholders + ")";
        return jdbc.query(sql, mapper, ids.toArray());
    }

    @Override
    public Map<Long, List<Genre>> getGenresByFilmIds(Collection<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT fg.film_id AS film_id, g.genre_id AS genre_id, g.name AS name " +
                "FROM film_genre fg " +
                "JOIN genres g ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id IN (" + placeholders + ") " +
                "ORDER BY fg.film_id, g.genre_id";

        Map<Long, List<Genre>> result = new LinkedHashMap<>();
        jdbc.query(sql, rs -> {
            long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
            result.computeIfAbsent(filmId, key -> new ArrayList<>()).add(genre);
        }, filmIds.toArray());
        return result;
    }

    @Override
    public void addFilmGenres(Long filmId, Collection<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }
        List<Integer> uniqueGenreIds = genreIds.stream().distinct().collect(Collectors.toList());
        jdbc.batchUpdate(ADD_FILM_GENRE, uniqueGenreIds, uniqueGenreIds.size(),
                (ps, genreId) -> {
                    ps.setLong(1, filmId);
                    ps.setInt(2, genreId);
                });
    }

    @Override
    public void deleteFilmGenres(Long filmId) {
        jdbc.update(DELETE_FILM_GENRES, filmId);
    }
}