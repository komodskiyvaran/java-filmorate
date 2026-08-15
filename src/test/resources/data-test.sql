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

INSERT INTO rating (name) VALUES
                              ('G'),
                              ('PG'),
                              ('PG-13'),
                              ('R'),
                              ('NC-17');

INSERT INTO genres (name) VALUES
                              ('Комедия'),
                              ('Драма'),
                              ('Мультфильм'),
                              ('Триллер'),
                              ('Документальный'),
                              ('Боевик');

INSERT INTO users (email, login, name, birthday) VALUES
                                                     ('user1@test.com', 'user1', 'Test User 1', '1990-01-01'),
                                                     ('user2@test.com', 'user2', 'Test User 2', '1995-05-15'),
                                                     ('user3@test.com', 'user3', 'Test User 3', '2000-12-31');

INSERT INTO films (name, description, release_date, duration, rating_id) VALUES
                                                                             ('Test Film 1', 'Description 1', '2020-01-01', 120, 1),
                                                                             ('Test Film 2', 'Description 2', '2021-06-15', 90, 2),
                                                                             ('Test Film 3', 'Description 3', '2022-12-31', 150, 3);

INSERT INTO film_genre (film_id, genre_id) VALUES
                                               (1, 1),
                                               (1, 2),
                                               (2, 3),
                                               (3, 4);

INSERT INTO film_likes (film_id, user_id) VALUES
                                              (1, 1),
                                              (1, 2),
                                              (2, 1);