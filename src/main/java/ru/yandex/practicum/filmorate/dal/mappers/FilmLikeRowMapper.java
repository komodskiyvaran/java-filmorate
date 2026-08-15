package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.FilmLike;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmLikeRowMapper implements RowMapper<FilmLike> {
    @Override
    public FilmLike mapRow(ResultSet rs, int rowNum) throws SQLException {
        FilmLike filmLike = new FilmLike();
        filmLike.setFilmId(rs.getLong("film_id"));
        filmLike.setUserId(rs.getLong("user_id"));
        return filmLike;
    }
}