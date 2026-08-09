package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaDbStorage.class, MpaRowMapper.class})
@Sql(scripts = {"classpath:schema.sql", "classpath:data-test.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    void shouldFindAllMpa() {
        List<Mpa> mpaList = mpaStorage.findAll();
        assertThat(mpaList).hasSize(5);
        assertThat(mpaList.get(0).getId()).isEqualTo(1);
        assertThat(mpaList.get(0).getName()).isEqualTo("G");
        assertThat(mpaList.get(4).getId()).isEqualTo(5);
        assertThat(mpaList.get(4).getName()).isEqualTo("NC-17");
    }

    @Test
    void shouldFindMpaById() {
        Optional<Mpa> mpa = mpaStorage.findById(1);
        assertThat(mpa).isPresent();
        assertThat(mpa.get().getId()).isEqualTo(1);
        assertThat(mpa.get().getName()).isEqualTo("G");
    }

    @Test
    void shouldReturnEmptyWhenMpaNotFound() {
        Optional<Mpa> mpa = mpaStorage.findById(999);
        assertThat(mpa).isEmpty();
    }
}