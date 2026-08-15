package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class BaseRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    public BaseRepository(JdbcTemplate jdbc, RowMapper<T> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    protected long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Failed to retrieve generated ID");
        }
        return key.longValue();
    }

    protected void update(String query, Object... params) {
        int rows = jdbc.update(query, params);
        if (rows == 0) {
            throw new RuntimeException("Failed to update data");
        }
    }

    protected boolean delete(String query, long id) {
        return jdbc.update(query, id) > 0;
    }
}