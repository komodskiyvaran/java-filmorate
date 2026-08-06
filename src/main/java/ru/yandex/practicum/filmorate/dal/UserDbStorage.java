package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;
import static ru.yandex.practicum.filmorate.exception.ErrorMessages.ID_MUST_BE_SPECIFIED;

@Slf4j
@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL = "SELECT * FROM users";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String DELETE = "DELETE FROM users WHERE user_id = ?";
    private static final String FIND_BY_EMAIL = "SELECT * FROM users WHERE email = ?";

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL);
    }

    @Override
    public User findById(long id) {
        return findOne(FIND_BY_ID, id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }

    @Override
    public User create(User user) {
        long id = insert(INSERT, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        user.setId(id);
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            User user = jdbc.queryForObject(FIND_BY_EMAIL, mapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public User update(User user) {
        update(UPDATE, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public void delete(long id) {
        delete(DELETE, id);
    }


   /* @Override
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

    @Override
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

    @Override
    public Collection<User> getFriends(long userId) {
        return findById(userId).getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(long userId, long otherId) {
        Set<Long> userFriends = findById(userId).getFriends();
        Set<Long> otherFriends = findById(otherId).getFriends();

        Set<Long> commonIds = new HashSet<>(userFriends);
        commonIds.retainAll(otherFriends);

        return commonIds.stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    private void updateFieldUser(User user, User updatedUser) {
        user.setEmail(updatedUser.getEmail());
        user.setLogin(updatedUser.getLogin());
        user.setName(updatedUser.getName());
        user.setBirthday(updatedUser.getBirthday());
    }*/

}
