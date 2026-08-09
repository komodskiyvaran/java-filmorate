-- Очистка
DELETE FROM film_likes;
DELETE FROM film_genre;
DELETE FROM friendship;
DELETE FROM films;
DELETE FROM users;
DELETE FROM genres;
DELETE FROM rating;

ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE rating ALTER COLUMN rating_id RESTART WITH 1;
ALTER TABLE genres ALTER COLUMN genre_id RESTART WITH 1;

-- Рейтинги
INSERT INTO rating (rating_id, name) VALUES (1, 'G');
INSERT INTO rating (rating_id, name) VALUES (2, 'PG');
INSERT INTO rating (rating_id, name) VALUES (3, 'PG-13');
INSERT INTO rating (rating_id, name) VALUES (4, 'R');
INSERT INTO rating (rating_id, name) VALUES (5, 'NC-17');

-- Жанры
INSERT INTO genres (genre_id, name) VALUES (1, 'Комедия');
INSERT INTO genres (genre_id, name) VALUES (2, 'Драма');
INSERT INTO genres (genre_id, name) VALUES (3, 'Мультфильм');
INSERT INTO genres (genre_id, name) VALUES (4, 'Триллер');
INSERT INTO genres (genre_id, name) VALUES (5, 'Документальный');
INSERT INTO genres (genre_id, name) VALUES (6, 'Боевик');

-- Пользователи
INSERT INTO users (user_id, email, login, name, birthday) VALUES
                                                              (1, 'user1@test.com', 'user1', 'Test User 1', '1990-01-01'),
                                                              (2, 'user2@test.com', 'user2', 'Test User 2', '1995-05-15'),
                                                              (3, 'user3@test.com', 'user3', 'Test User 3', '2000-12-31');

-- Фильмы
INSERT INTO films (film_id, name, description, release_date, duration, rating_id) VALUES
                                                                                      (1, 'Test Film 1', 'Description 1', '2020-01-01', 120, 1),
                                                                                      (2, 'Test Film 2', 'Description 2', '2021-06-15', 90, 2),
                                                                                      (3, 'Test Film 3', 'Description 3', '2022-12-31', 150, 3);

-- Жанры фильмов
INSERT INTO film_genre (film_id, genre_id) VALUES
                                               (1, 1),
                                               (1, 2),
                                               (2, 3),
                                               (3, 4);

-- Лайки
INSERT INTO film_likes (film_id, user_id) VALUES
                                              (1, 1),
                                              (1, 2),
                                              (2, 1);