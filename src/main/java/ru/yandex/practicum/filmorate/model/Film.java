package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.yandex.practicum.filmorate.validation.ValidReleaseDate;

import static ru.yandex.practicum.filmorate.exception.ErrorMessages.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private Long id;

    @NotBlank (message = FILM_NAME_EMPTY)
    private String name;

    @Size(max = 200, message = FILM_DESCRIPTION_TOO_LONG)
    private String description;

    @ValidReleaseDate
    private LocalDate releaseDate;

    @Positive(message = FILM_DURATION_NOT_POSITIVE)
    private Long duration;
}
