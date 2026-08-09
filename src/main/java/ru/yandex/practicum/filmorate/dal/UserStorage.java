package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    Optional<User> findByEmail(String email);

    User findById(long id);

    User update(User updatedUser);

    void delete(long id);

    void addFriend(long userId, long friendId);

    void removeFriend(long userId, long friendId);

    Collection<User> getFriends(long userId);

    Collection<User> getCommonFriends(long userId, long otherId);
}
