package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class,
        FilmRowMapper.class,
        FilmLikeRowMapper.class,
        GenreRowMapper.class,
        MpaRowMapper.class,
        UserRowMapper.class,
        UserDbStorage.class,
        GenreDbStorage.class,
        MpaDbStorage.class
})
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = {"classpath:data-test.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    // ==================== Тесты на получение ====================

    @Test
    void testGetAllFilms() {
        Collection<Film> films = filmStorage.findAll();
        assertThat(films).hasSize(3);
    }

    @Test
    void testFindById() {
        Optional<Film> filmOptional = filmStorage.findOne(
                "SELECT f.*, r.rating_id, r.name AS rating_name FROM films f " +
                        "LEFT JOIN rating r ON f.rating_id = r.rating_id WHERE f.film_id = ?", 1L
        );

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getId()).isEqualTo(1L);
                    assertThat(film.getName()).isEqualTo("Test Film 1");
                    assertThat(film.getDescription()).isEqualTo("Description 1");
                    assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(2020, 1, 1));
                    assertThat(film.getDuration()).isEqualTo(120);
                    assertThat(film.getMpa()).isNotNull();
                    assertThat(film.getMpa().getId()).isEqualTo(1L);
                    assertThat(film.getMpa().getName()).isEqualTo("G");
                });
    }

    @Test
    void testFindById_NotFound() {
        assertThrows(ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> filmStorage.findById(999L));
    }

    // ==================== Тесты на создание ====================

    @Test
    void testCreateFilm() {
        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("New Description");
        newFilm.setReleaseDate(LocalDate.of(2023, 1, 1));
        newFilm.setDuration(100L);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        newFilm.setMpa(mpa);

        Genre genre1 = new Genre();
        genre1.setId(1);
        Genre genre2 = new Genre();
        genre2.setId(2);
        newFilm.setGenres(List.of(genre1, genre2));

        Film created = filmStorage.create(newFilm);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("New Film");
        assertThat(created.getGenres()).hasSize(2);

        // Проверим, что фильм действительно сохранился
        Film found = filmStorage.findById(created.getId());
        assertThat(found).isNotNull();
        assertThat(found.getGenres()).extracting("id").containsExactlyInAnyOrder(1, 2);
    }

    // ==================== Тесты на обновление ====================

    @Test
    void testUpdateFilm() {
        Film film = filmStorage.findById(1L);
        film.setName("Updated Film");
        film.setDescription("Updated Description");
        film.setDuration(999L);

        // Обновляем жанры
        Genre genre3 = new Genre();
        genre3.setId(3);
        film.setGenres(List.of(genre3));

        Film updated = filmStorage.update(film);

        assertThat(updated.getName()).isEqualTo("Updated Film");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getDuration()).isEqualTo(999);
        assertThat(updated.getGenres()).extracting("id").containsExactly(3);
    }

    @Test
    void testUpdateFilm_NotFound() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Non Existent Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120L);
        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setMpa(mpa);

        assertThrows(RuntimeException.class, () -> filmStorage.update(film));
    }

    // ==================== Тесты на удаление ====================

    @Test
    void testDeleteFilm() {
        filmStorage.delete(3L);
        assertThrows(ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> filmStorage.findById(3L));
        Collection<Film> films = filmStorage.findAll();
        assertThat(films).hasSize(2);
    }

    @Test
    void testDeleteFilm_NotFound() {
        assertThrows(ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> filmStorage.delete(999L));
    }

    // ==================== Тесты на лайки ====================

    @Test
    void testAddLike() {
        // Проверяем, что лайк добавился
        filmStorage.addLike(3L, 1L);

        // Проверяем популярные фильмы (фильм 1 должен быть самым популярным)
        Collection<Film> popular = filmStorage.getPopularFilms(10);
        assertThat(popular).isNotEmpty();
        assertThat(popular.iterator().next().getId()).isEqualTo(1L);
    }

    @Test
    void testRemoveLike() {
        filmStorage.removeLike(1L, 1L);
        // Проверяем, что лайк удалён (проверка через getPopularFilms не даёт 100% гарантии,
        // но можно проверить через прямой запрос)
        Integer count = filmStorage.jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class, 1L, 1L
        );
        assertThat(count).isZero();
    }

    @Test
    void testGetPopularFilms() {
        Collection<Film> popular = filmStorage.getPopularFilms(2);
        assertThat(popular).hasSize(2);
        // Самый популярный фильм (2 лайка) — первый
        assertThat(popular.iterator().next().getId()).isEqualTo(1L);
    }

    @Test
    void testGetPopularFilms_WithLimit() {
        Collection<Film> popular = filmStorage.getPopularFilms(1);
        assertThat(popular).hasSize(1);
        assertThat(popular.iterator().next().getId()).isEqualTo(1L);
    }

    @Test
    void testGetPopularFilms_EmptyResult() {
        // Удаляем все лайки
        filmStorage.jdbc.update("DELETE FROM film_likes");
        Collection<Film> popular = filmStorage.getPopularFilms(10);
        // Должны вернуться все фильмы (без лайков)
        assertThat(popular).hasSize(3);
    }

    // ==================== Дополнительные проверки ====================

    @Test
    void testLoadMpaAndGenres() {
        Film film = filmStorage.findById(1L);
        assertThat(film.getMpa()).isNotNull();
        assertThat(film.getMpa().getId()).isEqualTo(1L);
        assertThat(film.getGenres()).hasSize(2);
        assertThat(film.getGenres()).extracting("id").containsExactlyInAnyOrder(1, 2);
    }
}