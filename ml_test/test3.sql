Create table movies (id INT UNIQUE, title TEXT, genres TEXT);
Create table ratings (user_id int, movie_id int, rating int, ratingtime INT);
Create table tags (user_id int, movie_id int, tag TEXT, tagtime LONG);

MAP movies TO 'movies.csv' DELIMITER ',' QUOTE '"' TYPE 'FILE' REMOVEHEADER;
--MAP ratings TO 'bzip2 -dc ratings.csv.bz2' DELIMITER ',' TYPE 'RAW';
MAP ratings TO 'ratings2.csv' DELIMITER ',' TYPE 'FILE';
MAP tags TO 'tags.csv' DELIMITER ',' QUOTE '"' TYPE 'FILE' REMOVEHEADER;

--Select title, sum(rating)/count(*) from ratings 
--hash inner join movies on movies.id = ratings.movie_id 
--hash inner join tags on tags.movie_id = ratings.movie_id
--where tag REGEXP '.*Nudity.*'
--group by title
--having count(*) > 10
--order by sum(rating)/count(*) desc
--LIMIT 100;

SELECT title, sum(rating)/count(*) from ratings
hash join movies on movies.id = ratings.movie_id
group by title
having count(*) > 20
order by sum(rating)/count(*) desc
LIMIT 10;

--SELECT count(ratings.ratingtime) from ratings;