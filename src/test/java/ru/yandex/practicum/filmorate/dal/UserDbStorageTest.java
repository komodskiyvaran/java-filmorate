package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
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

    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("test@test.com");
        assertThat(created.getLogin()).isEqualTo("testlogin");
        assertThat(created.getName()).isEqualTo("Test User");
        assertThat(created.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldFindUserById() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        User found = userStorage.findById(created.getId());
        assertThat(found).isNotNull();
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
        User user1 = new User();
        user1.setEmail("test1@test.com");
        user1.setLogin("login1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("test2@test.com");
        user2.setLogin("login2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1995, 5, 5));
        userStorage.create(user2);

        Collection<User> users = userStorage.findAll();
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("testlogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        created.setName("New Name");
        created.setEmail("new@test.com");
        userStorage.update(created);

        User updated = userStorage.findById(created.getId());
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void shouldDeleteUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        userStorage.delete(created.getId());

        assertThatThrownBy(() -> userStorage.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFindUserByEmail() {
        User user = new User();
        user.setEmail("unique@test.com");
        user.setLogin("uniquelogin");
        user.setName("Unique User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.create(user);

        Optional<User> found = userStorage.findByEmail("unique@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("unique@test.com");
    }

    @Test
    void shouldAddAndRemoveFriend() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User created1 = userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1995, 5, 5));
        User created2 = userStorage.create(user2);

        userStorage.addFriend(created1.getId(), created2.getId());

        Collection<User> friends1 = userStorage.getFriends(created1.getId());
        assertThat(friends1).hasSize(1);
        assertThat(friends1.iterator().next().getId()).isEqualTo(created2.getId());

        userStorage.removeFriend(created1.getId(), created2.getId());

        Collection<User> friendsAfterRemove = userStorage.getFriends(created1.getId());
        assertThat(friendsAfterRemove).isEmpty();
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User created1 = userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1995, 5, 5));
        User created2 = userStorage.create(user2);

        User user3 = new User();
        user3.setEmail("user3@test.com");
        user3.setLogin("user3");
        user3.setName("User 3");
        user3.setBirthday(LocalDate.of(2000, 1, 1));
        User created3 = userStorage.create(user3);

        userStorage.addFriend(created1.getId(), created3.getId());
        userStorage.addFriend(created2.getId(), created3.getId());

        Collection<User> common = userStorage.getCommonFriends(created1.getId(), created2.getId());
        assertThat(common).hasSize(1);
        assertThat(common.iterator().next().getId()).isEqualTo(created3.getId());
    }
}