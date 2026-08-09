package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(long id) {
        return userStorage.findById(id);
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException(USER_EMAIL_EMPTY);
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException(USER_LOGIN_EMPTY);
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException(USER_LOGIN_CONTAINS_SPACE);
        }

        Optional<User> existing = userStorage.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            throw new DuplicatedDataException(USER_EMAIL_BUSY);
        }

        normalizeName(user);
        return userStorage.create(user);
    }

    public User update(User updatedUser) {
        if (updatedUser.getId() == null) {
            throw new ValidationException(ID_MUST_BE_SPECIFIED);
        }

        User existing = userStorage.findById(updatedUser.getId());

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
            Optional<User> duplicate = userStorage.findByEmail(updatedUser.getEmail());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(existing.getId())) {
                throw new DuplicatedDataException(USER_EMAIL_BUSY);
            }
            existing.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getLogin() != null && !updatedUser.getLogin().isBlank()) {
            if (updatedUser.getLogin().contains(" ")) {
                throw new ValidationException(USER_LOGIN_CONTAINS_SPACE);
            }
            existing.setLogin(updatedUser.getLogin());
        }

        if (updatedUser.getName() != null) {
            existing.setName(updatedUser.getName());
        }

        if (updatedUser.getBirthday() != null) {
            existing.setBirthday(updatedUser.getBirthday());
        }

        normalizeName(existing);
        return userStorage.update(existing);
    }

    public void delete(long id) {
        userStorage.delete(id);
    }

    public void addFriend(long userId, long friendId) {
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getFriends(long userId) {
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(long userId, long otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }

    private void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}