package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private static final LocalDate DATE_NOW = LocalDate.now();
    private final List<User> users = new ArrayList<>();


    @GetMapping
    public List<User> findAll() {
        return users;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {

        validName(user);

        user.setId(getNextId());
        users.add(user);

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

        User user = findUserById(updatedUser.getId());
        if (user == null) {
            String message = String.format(USER_NOT_FOUND, updatedUser.getId());

            log.warn(message);
            throw new NotFoundException(message);
        }

        validName(updatedUser);

        user.setEmail(updatedUser.getEmail());
        user.setLogin(updatedUser.getLogin());
        user.setName(updatedUser.getName());
        user.setBirthday(updatedUser.getBirthday());

        log.info("User updated successfully: {} (id={})", user.getName(), user.getId());
        return user;
    }

    private long getNextId() {
        return users.stream().mapToLong(User::getId).max().orElse(0) + 1;
    }

    private User findUserById(long id) {
        return users.stream()
                .filter(u -> u.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private void validName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
