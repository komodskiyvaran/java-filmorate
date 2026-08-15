package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDbStorage.class, GenreRowMapper.class})
@Sql(
        scripts = {"classpath:schema.sql", "classpath:data-test.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreStorage;

    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).hasSize(6);
        assertThat(genres)
                .extracting(Genre::getId)
                .containsExactly(1, 2, 3, 4, 5, 6);
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
        assertThat(genreStorage.findById(999)).isEmpty();
    }

    @Test
    void shouldFindAllByIdsAndSilentlyDropUnknownIds() {
        List<Genre> genres = genreStorage.findAllByIds(List.of(1, 2, 999));

        assertThat(genres).extracting(Genre::getId).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void findAllByIdsShouldReturnEmptyListForEmptyInput() {
        assertThat(genreStorage.findAllByIds(Collections.emptyList())).isEmpty();
    }

    @Test
    void shouldGetGenresByFilmIdForSingleFilm() {
        Map<Long, List<Genre>> genresByFilm = genreStorage.getGenresByFilmIds(List.of(1L));

        assertThat(genresByFilm).containsOnlyKeys(1L);
        assertThat(genresByFilm.get(1L))
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void shouldGetGenresByFilmIdsForMultipleFilmsInOneQuery() {
        Map<Long, List<Genre>> genresByFilm = genreStorage.getGenresByFilmIds(List.of(1L, 2L));

        assertThat(genresByFilm).containsOnlyKeys(1L, 2L);
        assertThat(genresByFilm.get(1L)).extracting(Genre::getId).containsExactlyInAnyOrder(1, 2);
        assertThat(genresByFilm.get(2L)).extracting(Genre::getId).containsExactly(3);
    }

    @Test
    void getGenresByFilmIdsShouldOmitFilmsWithoutGenres() {
        genreStorage.deleteFilmGenres(3L);

        Map<Long, List<Genre>> genresByFilm =
                genreStorage.getGenresByFilmIds(List.of(3L));

        assertThat(genresByFilm).doesNotContainKey(3L);
    }

    @Test
    void getGenresByFilmIdsShouldReturnEmptyMapForEmptyInput() {
        assertThat(genreStorage.getGenresByFilmIds(Collections.emptyList())).isEmpty();
    }

    @Test
    void shouldAddFilmGenres() {
        genreStorage.addFilmGenres(2L, List.of(2));

        Map<Long, List<Genre>> genresByFilm = genreStorage.getGenresByFilmIds(List.of(2L));

        assertThat(genresByFilm.get(2L))
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(2, 3);
    }

    @Test
    void addFilmGenresShouldDeduplicateIds() {
        genreStorage.addFilmGenres(1L, List.of(4, 4, 4));

        Map<Long, List<Genre>> genresByFilm = genreStorage.getGenresByFilmIds(List.of(1L));

        assertThat(genresByFilm.get(1L))
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2, 4);
    }

    @Test
    void addFilmGenresShouldDoNothingForEmptyInput() {
        genreStorage.addFilmGenres(1L, Collections.emptyList());

        Map<Long, List<Genre>> genresByFilm = genreStorage.getGenresByFilmIds(List.of(1L));

        assertThat(genresByFilm.get(1L)).extracting(Genre::getId).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void shouldDeleteFilmGenres() {
        genreStorage.deleteFilmGenres(1L);

        Map<Long, List<Genre>> genresByFilm = genreStorage.getGenresByFilmIds(List.of(1L));

        assertThat(genresByFilm).doesNotContainKey(1L);
    }
}