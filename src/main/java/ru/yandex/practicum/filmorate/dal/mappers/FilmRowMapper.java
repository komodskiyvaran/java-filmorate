package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));
        film.setMpaId(rs.getLong("rating_id"));


        Long mpaId = film.getMpaId();
        if (mpaId != null && mpaId > 0) {
            try {
                String ratingName = rs.getString("rating_name");
                if (ratingName != null && !ratingName.isBlank()) {
                    Mpa mpa = new Mpa();
                    mpa.setId(mpaId);
                    mpa.setName(ratingName);
                    film.setMpa(mpa);
                }
            } catch (SQLException ignored) {
            }
        }

        return film;
    }
}
