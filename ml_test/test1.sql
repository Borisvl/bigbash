Create table movies (id INT UNIQUE, title TEXT, genres TEXT);
Create table ratings (user_id int, movie_id int, rating int, ratingtime LONG);
Create table users (user_id int UNIQUE, gender TEXT, age int, occupation int, zipcode Text);

MAP movies TO 'movies*.dat' DELIMITER '::';
MAP ratings TO 'bzip2 -dc ratings.dat.bz2' DELIMITER '::' TYPE 'RAW';
MAP users TO 'users.dat' DELIMITER '::' TYPE 'FILE';

--Select genres, count(*) from movies group by genres;
