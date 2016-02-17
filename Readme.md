BigBash
==========

BigBash is a SQL parser that converts _select_ statements to Bash one-liners that are executable directly 
on csv, log files, and other flat files. BigBash doesn't use a database, so the generated script should run on almost any \*nix device.  

You might find BigBash useful if:
- you don't have access to a database
- you're a sysop who wants to do simple aggregations without installing a database on your machine
- you're a Big Data skeptic who's interested in a tool that crunches a lot of data using only Bash scripts

Important Disclaimer
-----------------
BigBash is currently not meant for use in production or in a commercial setting. It can produce unexpected or incomplete results. The project originated during [Zalando Hack Week](https://tech.zalando.com/blog/?tags=Hack%20Week) and should be treated as an experiment. 

That said, if you'd like to contribute to enhancing the project's useability please see the guidelines and TODO below. 

Getting Started
-----------------

### Installation Requirements
- Java JDK >= 1.7
- Maven >= 3.0

BigBash is free of dependencies, so after installing the above you should be able to run it instantly on any operating system.

### Running
Download the source code. To launch the converter, use `./bigbash.sh`. This should automatically start the build process.

### Configuration
You can change which *nix programs BigBash uses by editing the `bigbash.conf` file. BigBash first looks in the
current directory for the config files. If it finds none, it chooses the paths `~/.config/bigbash.conf` and `/etc/bigbash.conf`.

### Supported SQL
BigBash supports a subset of SQL92 syntax for _select_ and _create table_ statements. Please note that the _\*_
operator, e.g. in _"SELECT \* FROM ..."_, sometimes shows unexpected results.

### Not Supported
* Sub-selects, UNION, IN
* CASE
* LIKE (use REGEXP instead)
* DISTINCT (you can use GROUP BY in some situations instead, though this will not work in some aggregation functions)
* Aliases, AS operator
* Joins that are not equi-joins
* Implicit joins (use explicit joins instead)
* Queries without a FROM part

### Special Syntax

BigBash only supports inner and left hash joins, as well as the _HASH JOIN_ operator. _HASH JOIN_ acts like a normal join, but is more performant in situations where the right side of the join fits completely into memory. For right or outer joins, use the normal join operation instead. If the join-key of the right table is marked "unique" in 
the corresponding _CREATE TABLE_ statement, the hash join operation is even faster.

### The MAP command

MAP **table_name** TO **'filename[s]'|'command'** [DELIMITER **'delimiter'**] [QUOTE **'quote_char'**] [TYPE **'type'**] [REMOVEHEADER]

#### Parameters

* **table_name**: The name of an existing table
* **filename[s]**: A globbing expression that denotes one or more files. Examples: _*.gz_, _access.log.2015-03-0[123].gz_. 
You can also use bash extended globbing patterns, if enabled via _shopt -s extglob_.
* **command** (If TYPE is set to 'RAW): A bash command that outputs the stream to be mapped to the table. Examples: 
_bzip2 -dc *.bz_
* DELIMITER: Specifies the character that separates the columns. The default is a tabulator.
* QUOTE: Specifies the character that is used to quote column expressions. Default is no quotation. Please note that
big-bash removes all quotations and it is necessary to choose an output delimiter that is not used in the mapped data.
* TYPE: Allows to specify the input type. **'type'** must be one of 'FILE' (default), 'GZ' or 'RAW'.
* REMOVEHEADER: Use this if the files contain a header, they will be ignored.

#### Caveats 

* Escaping of delimiters in the input files is not supported at the moment


How It Works
-----------------
### A First Example

Let's do a quick example how to use big-bash to query the Movielens dataset. This datasets contains ratings of movies
from various users and is made available for free by GroupLens. 
First, download the dataset and extract it via 

    wget "http://files.grouplens.org/datasets/movielens/ml-1m.zip"
    unzip ml-1m.zip
    
You should now have a directory ml-1m that contains three files:

* movies.dat: Title and genre information
* ratings.dat: The ratings from 1-5 with a timestamp
* users.dat: Additional user informations

Now, open an editor, type in

    CREATE TABLE movies (id INT UNIQUE, title TEXT, genres TEXT);
    MAP movies TO 'movies.dat' DELIMITER '::';
    SELECT title FROM movies ORDER BY title LIMIT 10;

and save it under ml_test1.sql. Type in your terminal

    ./bigbash.sh -f ml_test1.sql
    
and the program outputs a bash one-liner like this: 

    (trap "kill 0" SIGINT; export LC_ALL=C; cat movies.dat|sed 's/::/\t/g'|cut -d $'\t' -f2|sort -S2G --parallel=4 -t$'\t'  -k 1,1|head -n10|awk -F '\t' '{print $1}')

Executing this one-liner in the movielens directory should now produce the
first ten movies (alphabetical) of the dataset. Let us take a look at the created sql file. The first line uses a
typical _CREATE TABLE_ statement to define the table _movies_ with three columns. The next line

    MAP movies TO 'movies.dat' DELIMITER '::';
    
states that the table should be mapped to the file "movies.dat" and the delimiter used in this file are two colons. 
Since we are using a shell script to execute the query, general globbing pattern can be used instead of a simple 
filename.
Big-bash also supports gziped files as well as quotations. Look ***TODO*** for more details.

The third line is a standard sql _SELECT_ statement to output the movie names, which is translated to the bash 
one-liner. If you are familiar with bash scriptings and the unix tools, then it should be no problem for you to 
understand the created bash code.

### A second example

The previous example was rather basic and does not show the full power of the tool. Let us create a more complicated
statement on the dataset that uses also the ratings: The Top-10 movies according to the avg. ratings of all male 
users older than thirty. 
Similar to the previous example, we first create the tables and the mappings and then the _SELECT_ 
statement.
Open an editor and type

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
    HAVING count(*) > 10
    ORDER BY sum(rating)/count(*) DESC
    LIMIT 10;
    
and save it as ml_test2.sql. Executing it leads to a rather long one-liner similar to this one
    
    (trap "kill 0" SIGINT; export LC_ALL=C; 
    awk 'BEGIN{FS=OFS="\t"}NR==FNR{map[$1]=$0; next}{c=map[$1]; if (c) print $0,c; }' 
    <(cat users.dat|sed 's/::/\t/g'|cut -d $'\t' -f1,2,3|awk -F '\t' '($3 >= 30 && $2 == "M") {print}') 
    <(awk 'BEGIN{FS=OFS="\t"}NR==FNR{map[$1]=$0; next}{c=map[$2]; if (c) print $0,c; }' 
    <(cat movies.dat|sed 's/::/\t/g'|cut -d $'\t' -f1,2) <(cat ratings.dat|sed 's/::/\t/g'|cut -d $'\t' -f1,2,3))
    |awk -F '\t' '($8 >= 30 && $7 == "M") {print}'
    |awk -F '\t' 'BEGIN {OFS="\t"} {k=$5; row[k]=$0;sum_0[k] +=$3;count_1[k] +=1;} END{for (k in row) print row[k],sum_0[k],count_1[k]}'
    |awk -F '\t' '($10 > 5) {print}'|awk 'BEGIN{FS=OFS="\t"}{print $0,$9 / $10}'
    |sort -S2G --parallel=4 -t$'\t'  -k 11,11rn|head -n10|awk -F '\t' '{print $5"\t"$9 / $10}')

Since it is cumbersome to work with such a long script, we can also save the output directly into a file via

    echo "#!/usr/bin/env bash" > ml_test2.sh
    ./bigbash.sh -f ml_test2.sql >> ml_test2.sh
    chmod +x ml_test2.sh
    
Executing this in our directory leads to the following output (piped through `column -t -s $'\t'`):

    Seven Samurai (The Magnificent Seven) (Shichinin no samurai) (1954)          4.57915
    Carmen (1984)                                                                4.57143
    For All Mankind (1989)                                                       4.5625
    Godfather, The (1972)                                                        4.55674
    Casablanca (1942)                                                            4.54377
    Sanjuro (1962)                                                               4.53846
    Wrong Trousers, The (1993)                                                   4.52804
    To Kill a Mockingbird (1962)                                                 4.52548
    Schindler's List (1993)                                                      4.5022
    Dr. Strangelove or: How I Learned to Stop Worrying and Love the Bomb (1963)  4.50093

Here we have used _HASH JOIN_ which is special for Big-bash. It describes a normal inner join but uses a hash map
internally to store the values of the right-hand side. It is faster in cases you are joining a large table 
with a small one, like in this example. Note, that we also add a _UNIQUE_ constraint to the id column in the movie 
table. This also increases the performance of the hash join.
We could replace it via a normal join and would get the same output, but it would take 
more time to create the output.

Usage
---------------

    java -jar big-bash.jar [OPTION]... [SQL commands]...

    Options:
     --help            : Print this message (default: false)
     --noAnsiC         : Use this if you have problems with the default Ansi-C
                         decoding (default: false)
     --sortAggregation : Uses sort instead of hash based aggregation (default:
                         false)
     -d VAL            : Output delimiter (default: \t)
     -f (--file) FILE  : SQL file that should be processed

Supported functions
-----------------------

### Aggregation functions

* Count (with *DISTINCT* support)
* Sum
* Min
* Max
* group_concat (with *DISTINCT* support)

Note that when using the *DISTINCT* operator, it is assumed that all distinct values fit into memory.

### Logical operators

* <,>,>=,<=,<>,!=,=,==
* AND,OR

### Arithmetic operators

* +,-,*,/,% (modulo)

### Functions

All awk functions are supported. For more details please refer the 
[awk documentation](http://www.math.utah.edu/docs/info/gawk_13.html).

FAQ
------

Q: I cannot do anything when I have JSON files, right?

A: Big-Bash does not support JSON out of the box, but in some cases you can convert it to csv using sed or jq. 
Example: `cat persons.json | jq -r '[.name,.address.street,.address.city,.gender]|@csv'`
This can then be used together with the RAW mapping type.

Q: Can I directly invoke Big-Bash with files on S3?

A: You can do this using the aws tools. One way is to use a one-liner like this as input command

    AWS_BUCKET="s3://<bucketname>"; AWS_PATH_PREFIX="<path to look for files>"; AWS_PATTERN="<the regexp file pattern to match>"; aws s3 ls "$AWS_BUCKET/$AWS_PATH_PREFIX" --recursive | sed -n -E 's/.* +[0-9]+ +(.*)/\1/p' | grep "$AWS_PATTERN" | xargs -i aws s3 cp "$AWS_BUCKET/{}" - 

License
----------

    Copyright 2015 Zalando SE

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
