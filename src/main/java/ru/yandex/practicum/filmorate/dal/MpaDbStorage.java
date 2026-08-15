package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;


@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper;

    private static final String FIND_ALL = "SELECT * FROM rating ORDER BY rating_id";
    private static final String FIND_BY_ID = "SELECT * FROM rating WHERE rating_id = ?";

    @Override
    public List<Mpa> findAll() {
        return jdbc.query(FIND_ALL, mapper);
    }

    @Override
    public Optional<Mpa> findById(int id) {
        try {
            Mpa mpa = jdbc.queryForObject(FIND_BY_ID, mapper, id);
            return Optional.ofNullable(mpa);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}