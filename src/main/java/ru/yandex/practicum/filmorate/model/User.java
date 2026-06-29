package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;

    @NotBlank(message = USER_EMAIL_EMPTY)
    @Email(message = USER_EMAIL_INVALID)
    private String email;

    @NotBlank(message = USER_LOGIN_EMPTY)
    private String login;

    private String name;

    @PastOrPresent(message = USER_BIRTHDAY_IN_FUTURE)
    private LocalDate birthday;
}