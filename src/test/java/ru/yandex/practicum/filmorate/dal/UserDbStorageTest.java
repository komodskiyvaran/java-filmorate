package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mappers.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class, FriendshipRowMapper.class})
@Sql(scripts = {"classpath:schema.sql", "classpath:data-test.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    private User newUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login + " name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void shouldCreateUser() {
        User created = userStorage.create(newUser("test@test.com", "testlogin"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("test@test.com");
        assertThat(created.getLogin()).isEqualTo("testlogin");
        assertThat(created.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldFindUserById() {
        User created = userStorage.create(newUser("test@test.com", "testlogin"));

        User found = userStorage.findById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void shouldThrowNotFoundWhenUserMissing() {
        assertThatThrownBy(() -> userStorage.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFindAllUsers() {
        User u1 = userStorage.create(newUser("test1@test.com", "login1"));
        User u2 = userStorage.create(newUser("test2@test.com", "login2"));

        Collection<User> users = userStorage.findAll();

        assertThat(users).extracting(User::getId).contains(u1.getId(), u2.getId());
    }

    @Test
    void shouldUpdateUser() {
        User created = userStorage.create(newUser("test@test.com", "testlogin"));

        created.setName("New Name");
        created.setEmail("new@test.com");
        userStorage.update(created);

        User updated = userStorage.findById(created.getId());
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void shouldDeleteUser() {
        User created = userStorage.create(newUser("test@test.com", "testlogin"));

        userStorage.delete(created.getId());

        assertThatThrownBy(() -> userStorage.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFindUserByEmail() {
        userStorage.create(newUser("unique@test.com", "uniquelogin"));

        Optional<User> found = userStorage.findByEmail("unique@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("unique@test.com");
    }

    @Test
    void findByEmailShouldReturnEmptyWhenMissing() {
        assertThat(userStorage.findByEmail("nope@test.com")).isEmpty();
    }

    @Test
    void shouldAddAndRemoveFriend() {
        User user1 = userStorage.create(newUser("user1@test.com", "user1"));
        User user2 = userStorage.create(newUser("user2@test.com", "user2"));

        userStorage.addFriend(user1.getId(), user2.getId());

        Collection<User> friends1 = userStorage.getFriends(user1.getId());
        assertThat(friends1).extracting(User::getId).containsExactly(user2.getId());

        userStorage.removeFriend(user1.getId(), user2.getId());

        assertThat(userStorage.getFriends(user1.getId())).isEmpty();
    }

    @Test
    void addFriendShouldBeOneDirectional() {
        User user1 = userStorage.create(newUser("dir1@test.com", "dir1"));
        User user2 = userStorage.create(newUser("dir2@test.com", "dir2"));

        userStorage.addFriend(user1.getId(), user2.getId());

        assertThat(userStorage.getFriends(user2.getId())).isEmpty();
    }

    @Test
    void isFriendShouldReturnTrueWhenFriendshipExists() {
        User user1 = userStorage.create(newUser("k1@test.com", "k1"));
        User user2 = userStorage.create(newUser("k2@test.com", "k2"));
        userStorage.addFriend(user1.getId(), user2.getId());

        assertThat(userStorage.isFriend(user1.getId(), user2.getId())).isTrue();
    }

    @Test
    void isFriendShouldReturnFalseWhenNoFriendshipExists() {
        User user1 = userStorage.create(newUser("l1@test.com", "l1"));
        User user2 = userStorage.create(newUser("l2@test.com", "l2"));

        assertThat(userStorage.isFriend(user1.getId(), user2.getId())).isFalse();
    }

    @Test
    void isFriendShouldBeOneDirectional() {
        User user1 = userStorage.create(newUser("m1@test.com", "m1"));
        User user2 = userStorage.create(newUser("m2@test.com", "m2"));
        userStorage.addFriend(user1.getId(), user2.getId());

        assertThat(userStorage.isFriend(user2.getId(), user1.getId())).isFalse();
    }

    @Test
    void addFriendTwiceAtStorageLevelShouldFailOnPrimaryKeyNotDuplicatedDataException() {
        User user1 = userStorage.create(newUser("n1@test.com", "n1"));
        User user2 = userStorage.create(newUser("n2@test.com", "n2"));
        userStorage.addFriend(user1.getId(), user2.getId());

        assertThatThrownBy(() -> userStorage.addFriend(user1.getId(), user2.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = userStorage.create(newUser("user1@test.com", "user1c"));
        User user2 = userStorage.create(newUser("user2@test.com", "user2c"));
        User common = userStorage.create(newUser("user3@test.com", "user3c"));

        userStorage.addFriend(user1.getId(), common.getId());
        userStorage.addFriend(user2.getId(), common.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends).extracting(User::getId).containsExactly(common.getId());
    }
}