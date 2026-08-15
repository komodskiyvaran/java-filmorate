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

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmDbStorage.class,
        GenreDbStorage.class,
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
    private GenreDbStorage genreStorage;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void shouldFindAllFilms() {
        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(3);
    }

    @Test
    void shouldFindFilmByIdWithMpaButWithoutGenres() {
        Film film = filmStorage.findById(1L);

        assertThat(film.getId()).isEqualTo(1L);
        assertThat(film.getName()).isEqualTo("Test Film 1");
        assertThat(film.getDescription()).isEqualTo("Description 1");
        assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(film.getDuration()).isEqualTo(120L);

        assertThat(film.getMpa()).isNotNull();
        assertThat(film.getMpa().getId()).isEqualTo(1L);
        assertThat(film.getMpa().getName()).isEqualTo("G");

        assertThat(film.getGenres()).isEmpty();
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
        film.setMpaId(1L);

        Film created = filmStorage.create(film);
        Film saved = filmStorage.findById(created.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Film");
        assertThat(saved.getMpa().getId()).isEqualTo(1L);
    }

    @Test
    void createShouldNotTouchFilmGenresEvenIfGenresAreSetOnTheObject() {
        Film film = new Film();
        film.setName("Film With Ignored Genres");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(100L);
        film.setMpaId(1L);
        film.setGenres(List.of(new Genre(1, null)));

        Film created = filmStorage.create(film);

        Integer genreLinks = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_genre WHERE film_id = ?", Integer.class, created.getId());
        assertThat(genreLinks).isZero();
    }

    @Test
    void shouldUpdateFilmFieldsWithoutTouchingGenres() {
        Film film = filmStorage.findById(1L);
        film.setName("Updated Film");
        film.setDescription("Updated Description");
        film.setDuration(999L);

        filmStorage.update(film);

        Film updated = filmStorage.findById(1L);
        assertThat(updated.getName()).isEqualTo("Updated Film");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getDuration()).isEqualTo(999L);

        Integer genreLinks = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_genre WHERE film_id = ?", Integer.class, 1L);
        assertThat(genreLinks).isEqualTo(2);
    }

    @Test
    void shouldFailToUpdateMissingFilm() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Missing Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120L);
        film.setMpaId(1L);

        assertThatThrownBy(() -> filmStorage.update(film))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldDeleteFilmAndItsLikes() {
        genreStorage.deleteFilmGenres(1L);

        filmStorage.delete(1L);

        assertThatThrownBy(() -> filmStorage.findById(1L))
                .isInstanceOf(NotFoundException.class);

        Integer likes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ?", Integer.class, 1L);
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

        assertThat(filmStorage.hasLike(3L, 1L)).isTrue();
    }

    @Test
    void shouldRemoveLike() {
        filmStorage.removeLike(1L, 1L);

        assertThat(filmStorage.hasLike(1L, 1L)).isFalse();
    }

    @Test
    void hasLikeShouldReturnFalseWhenNoLikeExists() {
        assertThat(filmStorage.hasLike(2L, 999L)).isFalse();
    }

    @Test
    void shouldGetPopularFilmsOrderedByLikesWithMpaButWithoutGenres() {
        Collection<Film> popular = filmStorage.getPopularFilms(2);

        assertThat(popular).hasSize(2);

        Film first = popular.iterator().next();
        assertThat(first.getId()).isEqualTo(1L);
        assertThat(first.getMpa()).isNotNull();

        assertThat(first.getGenres()).isEmpty();
    }

    @Test
    void shouldReturnEmptyPopularFilmsForNonPositiveLimit() {
        assertThat(filmStorage.getPopularFilms(0)).isEmpty();
        assertThat(filmStorage.getPopularFilms(-1)).isEmpty();
    }
}