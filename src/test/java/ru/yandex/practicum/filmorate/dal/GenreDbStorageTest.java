package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDbStorage.class, GenreRowMapper.class})
@Sql(scripts = {"classpath:schema.sql", "classpath:data-test.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreStorage;

    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();
        assertThat(genres).hasSize(6);
        assertThat(genres.get(0).getId()).isEqualTo(1);
        assertThat(genres.get(0).getName()).isEqualTo("Комедия");
        assertThat(genres.get(5).getId()).isEqualTo(6);
        assertThat(genres.get(5).getName()).isEqualTo("Боевик");
    }

    @Test
    void shouldFindGenreById() {
        Optional<Genre> genre = genreStorage.findById(1);
        assertThat(genre).isPresent();
        assertThat(genre.get().getId()).isEqualTo(1);
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }

    @Test
    void shouldReturnEmptyWhenGenreNotFound() {
        Optional<Genre> genre = genreStorage.findById(999);
        assertThat(genre).isEmpty();
    }
}