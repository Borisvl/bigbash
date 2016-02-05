CREATE TABLE movies (id INT UNIQUE, title TEXT, genres TEXT);
CREATE TABLE ratings (user_id int, movie_id int, rating int, ratingtime LONG);
CREATE TABLE users (id int UNIQUE, gender TEXT, age int, occupation int, zipcode Text);

MAP movies TO 'movies.dat' DELIMITER '::';
MAP ratings TO 'ratings.dat' DELIMITER '::';
MAP users TO 'users.dat' DELIMITER '::';

SELECT title, sum(rating)/count(*) from ratings 
hash JOIN movies on movies.id=ratings.movie_id
hash JOIN users on users.id=ratings.user_id
WHERE age >= 30 and gender ='M'
GROUP BY title
HAVING count(*) > 5
ORDER BY sum(rating)/count(*) DESC
LIMIT 10;
