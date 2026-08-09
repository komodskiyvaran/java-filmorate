package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmDbStorage.class,
        FilmRowMapper.class,
        GenreRowMapper.class,
        MpaRowMapper.class
})
@Sql(
        scripts = {"classpath:schema.sql", "classpath:data-test.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void shouldFindAllFilms() {
        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(3);
    }

    @Test
    void shouldFindFilmById() {
        Film film = filmStorage.findById(1L);

        assertThat(film.getId()).isEqualTo(1L);
        assertThat(film.getName()).isEqualTo("Test Film 1");
        assertThat(film.getDescription()).isEqualTo("Description 1");
        assertThat(film.getReleaseDate())
                .isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(film.getDuration()).isEqualTo(120L);

        assertThat(film.getMpa()).isNotNull();
        assertThat(film.getMpa().getId()).isEqualTo(1L);
        assertThat(film.getMpa().getName()).isEqualTo("G");

        assertThat(film.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void shouldThrowWhenFilmNotFound() {
        assertThatThrownBy(() -> filmStorage.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldCreateFilm() {
        Film film = new Film();
        film.setName("New Film");
        film.setDescription("New Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(100L);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setMpa(mpa);

        Genre comedy = new Genre();
        comedy.setId(1);

        Genre drama = new Genre();
        drama.setId(2);

        film.setGenres(List.of(comedy, drama));

        Film created = filmStorage.create(film);
        Film saved = filmStorage.findById(created.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Film");
        assertThat(saved.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void shouldUpdateFilm() {
        Film film = filmStorage.findById(1L);
        film.setName("Updated Film");
        film.setDescription("Updated Description");
        film.setDuration(999L);

        Genre genre = new Genre();
        genre.setId(3);
        film.setGenres(List.of(genre));

        filmStorage.update(film);

        Film updated = filmStorage.findById(1L);

        assertThat(updated.getName()).isEqualTo("Updated Film");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getDuration()).isEqualTo(999L);
        assertThat(updated.getGenres())
                .extracting(Genre::getId)
                .containsExactly(3);
    }

    @Test
    void shouldFailToUpdateMissingFilm() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Missing Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120L);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setMpa(mpa);

        assertThatThrownBy(() -> filmStorage.update(film))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldDeleteFilmWithRelations() {
        filmStorage.delete(1L);

        assertThatThrownBy(() -> filmStorage.findById(1L))
                .isInstanceOf(NotFoundException.class);

        Integer genreLinks = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_genre WHERE film_id = ?",
                Integer.class,
                1L
        );

        Integer likes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ?",
                Integer.class,
                1L
        );

        assertThat(genreLinks).isZero();
        assertThat(likes).isZero();
    }

    @Test
    void shouldFailToDeleteMissingFilm() {
        assertThatThrownBy(() -> filmStorage.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldAddLike() {
        filmStorage.addLike(3L, 1L);

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                3L,
                1L
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldRemoveLike() {
        filmStorage.removeLike(1L, 1L);

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                1L,
                1L
        );

        assertThat(count).isZero();
    }

    @Test
    void shouldGetPopularFilms() {
        Collection<Film> popular = filmStorage.getPopularFilms(2);

        assertThat(popular).hasSize(2);
        assertThat(popular.iterator().next().getId()).isEqualTo(1L);

        Film first = popular.iterator().next();

        assertThat(first.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void shouldReturnEmptyPopularFilmsForNonPositiveLimit() {
        assertThat(filmStorage.getPopularFilms(0)).isEmpty();
        assertThat(filmStorage.getPopularFilms(-1)).isEmpty();
    }
}