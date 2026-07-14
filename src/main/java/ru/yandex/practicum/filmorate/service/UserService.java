package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.USER_ALREADY_FRIEND;
import static ru.yandex.practicum.filmorate.exception.ErrorMessages.USER_NOT_FRIEND;

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

    public void addFriend(long userId, long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        if (user.getFriends().contains(friendId) && friend.getFriends().contains(userId)) {
            log.warn(USER_ALREADY_FRIEND);
            return;
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Users with IDs {} and {} became friends.", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        if (user.getFriends().contains(friendId) && friend.getFriends().contains(userId)) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
            log.info("Users with IDs {} and {} are no longer friends",  userId, friendId);
        } else {
            String message = String.format(USER_NOT_FRIEND, userId, friendId);
            log.warn(message);
        }
    }

    public Collection<User> getFriends(long userId) {
        return findById(userId).getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(long userId, long otherId) {
        Set<Long> userFriends = findById(userId).getFriends();
        Set<Long> otherFriends = findById(otherId).getFriends();

        Set<Long> commonIds = new HashSet<>(userFriends);
        commonIds.retainAll(otherFriends);

        return commonIds.stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }
}