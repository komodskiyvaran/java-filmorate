package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;
import static ru.yandex.practicum.filmorate.exception.ErrorMessages.ID_MUST_BE_SPECIFIED;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0;

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("User created successfully: {} (id={})", user.getName(), user.getId());
        return user;
    }

    @Override
    public User findById(long id) {
        return users.values().stream()
                .filter(user -> user.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
    }

    @Override
    public User update(User updatedUser) {
        log.debug("Request to update user: {}", updatedUser);

        if (updatedUser.getId() == null) {
            log.warn("Update failed: " + ID_MUST_BE_SPECIFIED);
            throw new ValidationException(ID_MUST_BE_SPECIFIED);
        }

        User user = users.get(updatedUser.getId());
        if (user == null) {
            String message = USER_NOT_FOUND + updatedUser.getId();
            log.warn(message);
            throw new NotFoundException(message);
        }
        updateFieldUser(user, updatedUser);

        log.info("User updated successfully: {} (id={})", user.getName(), user.getId());
        return user;
    }

    @Override
    public void delete(long id) {
        findById(id);
        log.info("The user with id = {} has been successfully deleted", id);
        users.remove(id);
    }

    private void updateFieldUser(User user, User updatedUser) {
        user.setEmail(updatedUser.getEmail());
        user.setLogin(updatedUser.getLogin());
        user.setName(updatedUser.getName());
        user.setBirthday(updatedUser.getBirthday());
    }

    private long getNextId() {
        return ++currentId;
    }
}
