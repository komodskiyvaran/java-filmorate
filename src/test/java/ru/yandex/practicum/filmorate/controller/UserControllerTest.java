package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    @Test
    void shouldThrowWhenUserIsEmpty() {
        User user = new User();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.checkUser(user)
        );

        assertEquals(USER_EMAIL_EMPTY, exception.getMessage());
    }

    @Test
    void shouldCreateUserWithValidData() {
        User user = User.builder()
                .email("test@ya.ru")
                .login("catlover")
                .name("Cat Lover")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertDoesNotThrow(() -> controller.checkUser(user));
    }

    @Test
    void shouldThrowWhenEmailIsEmpty() {
        User user = User.builder()
                .email("")
                .login("catlover")
                .name("Cat Lover")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.checkUser(user)
        );

        assertEquals(USER_EMAIL_EMPTY, exception.getMessage());
    }

    @Test
    void shouldThrowWhenEmailDoesNotContainAt() {
        User user = User.builder()
                .email("testmail.ru")
                .login("catlover")
                .name("Cat Lover")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.checkUser(user)
        );

        assertEquals(USER_EMAIL_INVALID, exception.getMessage());
    }

    @Test
    void shouldThrowWhenLoginIsEmpty() {
        User user = User.builder()
                .email("test@ya.ru")
                .login("")
                .name("Cat Lover")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.checkUser(user)
        );

        assertEquals(USER_LOGIN_EMPTY, exception.getMessage());
    }

    @Test
    void shouldSetNameToLoginWhenNameIsNull() {
        User user = User.builder()
                .email("test@ya.ru")
                .login("catlover")
                .name(null)
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        controller.checkUser(user);

        assertEquals("catlover", user.getName());
    }

    @Test
    void shouldThrowWhenBirthdayInFuture() {
        User user = User.builder()
                .email("test@ya.ru")
                .login("catlover")
                .name("Cat Lover")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.checkUser(user)
        );

        assertEquals(USER_BIRTHDAY_IN_FUTURE, exception.getMessage());
    }

    @Test
    void shouldAllowNullBirthday() {
        User user = User.builder()
                .email("test@ya.ru")
                .login("catlover")
                .name("Cat Lover")
                .birthday(null)
                .build();

        assertDoesNotThrow(() -> controller.checkUser(user));
    }

    @Test
    void shouldAllowBirthdayToday() {
        User user = User.builder()
                .email("test@ya.ru")
                .login("catlover")
                .name("Cat Lover")
                .birthday(LocalDate.now())
                .build();

        assertDoesNotThrow(() -> controller.checkUser(user));
    }
}