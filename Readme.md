BigBash
==========

BigBash is a SQL parser that converts _select_ statements to Bash one-liners that are executable directly 
on csv, log files, and other flat files. BigBash doesn't use a database, so the generated script should run on almost any \*nix device.  

You might find BigBash useful if:
- you don't have access to a database
- you're a sysop who wants to do simple aggregations without installing a database on your machine
- you're a Big Data skeptic who likes to crunch gigabytes of data using shell scripts

Important Disclaimer
-----------------
BigBash is currently not meant for use in production or in a commercial setting. 
It can produce unexpected or incomplete results. 
The project originated during [Zalando Hack Week](https://tech.zalando.com/blog/?tags=Hack%20Week) and should be treated as an experiment. 

That said, if you'd like to contribute to enhancing the project's useability please see the [developers section](#Developers-section). 

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
current directory for the config file. If it finds none, it chooses the paths `~/.config/bigbash.conf` and `/etc/bigbash.conf`.

### Example #1: Simple query on a file
Let's have BigBash query the [Movielens](https://movielens.org/) dataset, which contains user-generated movie ratings. Download the dataset and extract it via:

    wget "http://files.grouplens.org/datasets/movielens/ml-1m.zip"
    unzip ml-1m.zip
    
You should now have a directory ml-1m that contains three files:
* movies.dat: Title and genre information
* ratings.dat: The ratings (from one to five) and timestamps
* users.dat: Additional user information

Open an editor and type:

    CREATE TABLE movies (id INT UNIQUE, title TEXT, genres TEXT);
    MAP movies TO 'movies.dat' DELIMITER '::';
    SELECT title FROM movies ORDER BY title LIMIT 10;

Then save it under _ml_test1.sql_. In your terminal, type:

    ./bigbash.sh -f ml_test1.sql
    
The program should output a Bash one-liner like this: 

    (trap "kill 0" SIGINT; export LC_ALL=C; cat movies.dat|sed 's/::/\t/g'|cut -d $'\t' -f2|sort -S2G --parallel=4 -t$'\t'  -k 1,1|head -n10|awk -F '\t' '{print $1}')

Executing this one-liner in the Movielens directory should produce an alphabetically sorted list of the first 10 movies in the dataset. 

Let's take a look at the SQL file. The first line uses a typical _CREATE TABLE_ statement to define the table _movies_ with three columns. The next line:

    MAP movies TO 'movies.dat' DELIMITER '::';
    
states that the table should be mapped to the file _movies.dat_. The column delimiter used in this file are two colons. Because we are using a shell script to execute the query, we can also use a general globbing pattern instead of a simple 
filename.

BigBash also supports gziped files as well as quotations. Check the [MAP command](#The-MAP-Command) for more details.

The third line is a standard SQL _SELECT_ statement. It outputs the movie names, which are then translated to the Bash 
one-liner. If you are familiar with Bash scriptings and UNIX tools, understanding this Bash code shouldn't be a problem. 

### Example #2: Joining a Large Table to a Small One 
The above example doesn't show BigBash's full power. 
Let's create a more complicated statement on the dataset: Finding the top ten movies sorted by the average ratings submitted by all male users age 30 or above. 

As in the previous example, we first create the tables, mappings, and the _SELECT_ statement. Open an editor and type:

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
    
Save it as _ml_test2.sql_. Executing it leads to a rather long one-liner similar to this:
    
    (trap "kill 0" SIGINT; export LC_ALL=C; 
    awk 'BEGIN{FS=OFS="\t"}NR==FNR{map[$1]=$0; next}{c=map[$1]; if (c) print $0,c; }' 
    <(cat users.dat|sed 's/::/\t/g'|cut -d $'\t' -f1,2,3|awk -F '\t' '($3 >= 30 && $2 == "M") {print}') 
    <(awk 'BEGIN{FS=OFS="\t"}NR==FNR{map[$1]=$0; next}{c=map[$2]; if (c) print $0,c; }' 
    <(cat movies.dat|sed 's/::/\t/g'|cut -d $'\t' -f1,2) <(cat ratings.dat|sed 's/::/\t/g'|cut -d $'\t' -f1,2,3))
    |awk -F '\t' '($8 >= 30 && $7 == "M") {print}'
    |awk -F '\t' 'BEGIN {OFS="\t"} {k=$5; row[k]=$0;sum_0[k] +=$3;count_1[k] +=1;} END{for (k in row) print row[k],sum_0[k],count_1[k]}'
    |awk -F '\t' '($10 > 5) {print}'|awk 'BEGIN{FS=OFS="\t"}{print $0,$9 / $10}'
    |sort -S2G --parallel=4 -t$'\t'  -k 11,11rn|head -n10|awk -F '\t' '{print $5"\t"$9 / $10}')

It's cumbersome to work with such a long script, so you can save the output directly into a file via:

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

Here we see the _HASH JOIN_ operator at work. We also add a _UNIQUE_ constraint to the id column in the movie table; this, like _HASH JOIN_, creates a more performant script. 
Replacing it via a normal join would produce the same output, but usually takes longer.

Usage
---------------

    java -jar big-bash.jar [OPTION]... [SQL commands]...

    Options:
     --help            : Print this message (default: false)
     --noAnsiC         : Use this if you have problems with the default Ansi-C
                         decoding (default: false)
     --sortAggregation : Uses sort instead of hash-based aggregation (default:
                         false)
     -d VAL            : Output delimiter (default: \t)
     -f (--file) FILE  : SQL file that should be processed

Supported SQL
-----------------------

BigBash supports a subset of SQL92 syntax for _select_ and _create table_ statements. Please note that the _\*_
operator, e.g. in _"SELECT \* FROM ..."_, sometimes shows unexpected results.

### Not Supported
* Sub-selects, UNION, IN
* CASE
* LIKE (use REGEXP instead)
* DISTINCT (you can use GROUP BY, though this will not work in some aggregation functions)
* Aliases, AS operator
* Joins that are not equi-joins
* Implicit joins (use explicit joins instead)
* Queries without a FROM part

### Special Syntax
In addition to a standard _JOIN_ operator BigBash also supports a special _HASH JOIN_ operator.
_HASH JOIN_ acts like a normal join, but uses a hash map internally to store the values of the right-hand side. 
It's faster/more performant in situations where the right side of the join fits completely into memory. 
Note that only inner and left hash joins are supported, for right or outer joins use the normal join operation instead. 

If the join-key of the right table is marked "unique" in the corresponding _CREATE TABLE_ statement, the hash join operation is even faster.

### The MAP Command
MAP **table_name** TO **'filename[s]'|'command'** [DELIMITER **'delimiter'**] [QUOTE **'quote_char'**] [TYPE **'type'**] [REMOVEHEADER]

#### Parameters
* **table_name**: The name of an existing table
* **filename[s]**: A globbing expression that denotes one or more files. Examples: _*.gz_, and _access.log.2015-03-0[123].gz_. You can also use Bash-extended globbing patterns, if enabled via _shopt -s extglob_.
* **command** (If TYPE is set to `RAW`): A Bash command that outputs to the table the stream you wish to map. Example: 
_bzip2 -dc *.bz_.
* **DELIMITER**: Specifies the character that separates the columns. The default is a tabulator.
* **QUOTE**: Specifies the character used to quote column expressions. The default is no-quotation. BigBash removes all quotations, so you have to choose an output delimiter that isn't used in the mapped data.
* **TYPE**: Allows you to specify the input type. **'type'** must be one of these: 'FILE' (default), 'GZ' or 'RAW'.
* **REMOVEHEADER**: Use this if the files contain a header; otherwise they will be ignored.

#### Caveats 
* BigBash currently doesn't support escaping of delimiters in input files.

### Aggregation Functions

* Count (with *DISTINCT* support)
* Sum
* Min
* Max
* group_concat (with *DISTINCT* support)

Note that when using the *DISTINCT* operator, it is assumed that all distinct values fit into memory.

### Logical Operators

* <,>,>=,<=,<>,!=,=,==
* AND, OR

### Arithmetic Operators

* +,-,*,/,% (modulo)

### Functions

BigBash supports all awk functions. 
For more details, please refer to the [awk documentation](http://www.math.utah.edu/docs/info/gawk_13.html).


FAQ
---

Q: Bigbash is slow on my machine, what can I do to make it faster?

A: Using the `--parallel=<nr of cores>` switch for the sort makes a big difference in most cases (change it in your bigbash.conf).
The performance of the queries depends on the tools used. Make sure you have updated sort, awk, sed and join to 
the latest versions. In particular on Mac OS, the default installed core utils are really outdated 
(look [here](http://apple.stackexchange.com/questions/69223/how-to-replace-mac-os-x-utilities-with-gnu-core-utilities) 
for details how to update). Also try different awk implementations, e.g., mawk or gawk.

Q: I can't use BigBash with JSON files, can I?
A: BigBash does not support JSON out of the box, but in some cases you can convert JSON to csv using sed or jq. An example: `cat persons.json | jq -r '[.name,.address.street,.address.city,.gender]|@csv'`, which could be used together with the RAW mapping type.

Q: Can I directly invoke BigBash with files on S3?
A: Yes, using the AWS tools. One way is to use a one-liner like this as an input command:

    AWS_BUCKET="s3://<bucketname>"; AWS_PATH_PREFIX="<path to look for files>"; AWS_PATTERN="<the regexp file pattern to match>"; aws s3 ls "$AWS_BUCKET/$AWS_PATH_PREFIX" --recursive | sed -n -E 's/.* +[0-9]+ +(.*)/\1/p' | grep "$AWS_PATTERN" | xargs -i aws s3 cp "$AWS_BUCKET/{}" - 

Developers Section
-----------------
### Testing
There is an extensive list of integration tests that you can find in the file 
`src/test/resources/integrationTests`. To run the tests on your local machine execute
    
    mvn verify -Pintegration-tests

If these test fail it is very likely that BigBash will not work correctly on your machine,
most likely due to incompatibility issues with your \#nix tools.

### Contributing
We welcome code contributions, please just clone the repository and create pull requests.

TODO
----------
To contribute to BigBash, submit a pull request using the usual method. Here are some desired enhancements you could work on:
- enable BigBash to support the `PARALLEL` command and parallel execution
- provide aliases support
- ensure correct formatting of the data/error-checking
