package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.USER_NOT_FOUND;

@Slf4j
@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private static final String FIND_ALL = "SELECT * FROM users";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String FIND_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String INSERT = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String DELETE = "DELETE FROM users WHERE user_id = ?";
    private static final String ADD_FRIEND = "INSERT INTO friendship (from_user_id, to_user_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND = "DELETE FROM friendship WHERE from_user_id = ? AND to_user_id = ?";
    private static final String GET_FRIENDS = "SELECT u.* FROM users u " +
            "JOIN friendship f ON u.user_id = f.to_user_id WHERE f.from_user_id = ?";
    private static final String GET_COMMON_FRIENDS = "SELECT u.* FROM users u " +
            "JOIN friendship f1 ON u.user_id = f1.to_user_id AND f1.from_user_id = ? " +
            "JOIN friendship f2 ON u.user_id = f2.to_user_id AND f2.from_user_id = ?";
    private static final String CHECK_FRIEND_EXISTS = "SELECT COUNT(*) FROM friendship WHERE from_user_id = ? AND to_user_id = ?";

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
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findOne(FIND_BY_EMAIL, email);
    }

    @Override
    public User create(User user) {
        long id = insert(INSERT, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        update(UPDATE, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public void delete(long id) {
        findById(id);
        delete(DELETE, id);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        jdbc.update(ADD_FRIEND, userId, friendId);
        log.info("User {} added friend {}", userId, friendId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        int deleted = jdbc.update(DELETE_FRIEND, userId, friendId);
        log.info("Removed {} friendship row(s) for {} -> {}", deleted, userId, friendId);
    }

    @Override
    public boolean isFriend(long userId, long friendId) {
        Integer count = jdbc.queryForObject(CHECK_FRIEND_EXISTS, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    @Override
    public Collection<User> getFriends(long userId) {
        return jdbc.query(GET_FRIENDS, mapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(long userId, long otherId) {
        return jdbc.query(GET_COMMON_FRIENDS, mapper, userId, otherId);
    }
}