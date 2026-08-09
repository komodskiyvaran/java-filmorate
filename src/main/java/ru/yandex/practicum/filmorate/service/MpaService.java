package ru.yandex.practicum.filmorate.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaDbStorage mpaStorage;

    public List<Mpa> findAll() {
        return mpaStorage.findAll();
    }

    public Mpa findById(int id) {
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Mpa not found with id: " + id));
    }
}