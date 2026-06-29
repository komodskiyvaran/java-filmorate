package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0;

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validName(user);

        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("User created successfully: {} (id={})", user.getName(), user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User updatedUser) {
        log.debug("Request to update user: {}", updatedUser);

        if (updatedUser.getId() == null) {
            log.warn("Update failed: " + ID_MUST_BE_SPECIFIED);
            throw new ValidationException(ID_MUST_BE_SPECIFIED);
        }

        User user = users.get(updatedUser.getId());
        if (user == null) {
            String message = String.format(USER_NOT_FOUND, updatedUser.getId());
            log.warn(message);
            throw new NotFoundException(message);
        }
        updateFieldUser(user, updatedUser);

        log.info("User updated successfully: {} (id={})", user.getName(), user.getId());
        return user;
    }

    private void updateFieldUser(User user, User updatedUser) {
        validName(updatedUser);
        user.setEmail(updatedUser.getEmail());
        user.setLogin(updatedUser.getLogin());
        user.setName(updatedUser.getName());
        user.setBirthday(updatedUser.getBirthday());
    }

    private void validName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private long getNextId() {
        return ++currentId;
    }
}