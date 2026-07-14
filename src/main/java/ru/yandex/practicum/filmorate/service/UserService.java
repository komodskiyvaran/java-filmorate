package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    public User addFriend(long userId, long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Users with IDs {} and {} became friends.", userId, friendId);

        return friend;
    }

    public void removeFriend(long userId, long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        if (user.getFriends().contains(friendId) && friend.getFriends().contains(userId)) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
            log.info("Users with IDs {} and {} are no longer friends",  userId, friendId);
        } else {
            String message = String.format("Users with IDs %d and %d were not friends!", userId, friendId);
            log.warn(message);
            throw new NotFoundException(message);
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