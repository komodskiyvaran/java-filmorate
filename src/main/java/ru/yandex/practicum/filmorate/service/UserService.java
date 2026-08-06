package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.UserStorage;

import java.util.Collection;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.USER_EMAIL_BUSY;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(long id) {
         return userStorage.findById(id);
    }

    public User create(User user) {
        if (userStorage.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicatedDataException(USER_EMAIL_BUSY);
        }
        validName(user);
        return userStorage.create(user);
    }

    public User update(User updatedUser) {
        validName(updatedUser);
        return userStorage.update(updatedUser);
    }

    public void delete(long id) {
        userStorage.delete(id);
    }

    private void validName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

/*    public void addFriend(long userId, long friendId) {
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
    }*/
}