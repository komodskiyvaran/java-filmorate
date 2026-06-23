package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    @NonNull
    private String email;
    @NonNull
    private String login;
    private LocalDate birthday;
    private String name;
}
