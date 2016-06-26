grammar BashSql;

parse
 : ( sql_stmt_list )* EOF
 ;

error
 : UNEXPECTED_CHAR 
   { 
     throw new RuntimeException("UNEXPECTED_CHAR=" + $UNEXPECTED_CHAR.text); 
   }
 ;

sql_stmt_list
 : ';'* sql_stmt ( ';'+ sql_stmt )* ';'*
 ;

sql_stmt
 : (                                     create_table_stmt
                                       | select_stmt
                                       | map_stmt
                                       )
 ;

map_stmt 
 : K_MAP table_name K_TO to=STRING_LITERAL 
   (K_DELIMITER delimiter=STRING_LITERAL)? 
   (K_QUOTE quote=STRING_LITERAL)?
   (K_TYPE type=STRING_LITERAL)?
   (K_REMOVEHEADER)?
   ;

create_table_stmt
 : K_CREATE K_TABLE 
   ( database_name '.' )? table_name
   ( '(' column_def ( ',' column_def )* ( ',' table_constraint )* ')'
   )
 ;

create_view_stmt
 : K_CREATE ( K_TEMP | K_TEMPORARY )? K_VIEW ( K_IF K_NOT K_EXISTS )?
   ( database_name '.' )? view_name K_AS select_stmt
 ;


select_stmt
 : K_SELECT ( K_DISTINCT | K_ALL )? result_column ( ',' result_column )*
      from_statement?
      where_operator?
      group_by_operator?
      order_by_operator?
      limit_operator?
 ;

order_by_operator
 : K_ORDER K_BY ordering_term ( ',' ordering_term )* 
 ;
 
limit_operator
 : K_LIMIT expr ( ( K_OFFSET | ',' ) expr )?
 ;
 
column_def
 : column_name type_name?
 ;

type_name
 : name+ ( '(' signed_number ')'
         | '(' signed_number ',' signed_number ')' )?
 ;

/*
    SQLite understands the following binary operators, in order from highest to
    lowest precedence:

    ||
    *    /    %
    +    -
    <<   >>   &    |
    <    <=   >    >=
    =    ==   !=   <>   IS   IS NOT   IN   LIKE   GLOB   MATCH   REGEXP
    AND
    OR
*/

expr
 : literal_value                                                        #somethine
 | (table=table_name '.' )? column=column_name                          #column_name_def
 | op=( '-' | '+' ) arg1=expr                                           #unary_expression
 | arg1=expr op=( '*' | '/' | '%' ) arg2=expr                           #boolean_expression
 | arg1=expr op=( '+' | '-' ) arg2=expr                                 #boolean_expression
 | arg1=expr op=( '<' | '<=' | '>' | '>=' ) arg2=expr                   #boolean_expression
 | arg1=expr op=('=' | '==' | '<>' | '!=' | K_REGEXP) arg2=expr         #boolean_expression
 | arg1=expr op=K_AND arg2=expr                                         #boolean_expression
 | arg1=expr op=K_OR arg2=expr                                          #boolean_expression
 | function_name '(' ( K_DISTINCT? expr ( ',' expr )* | '*' )? ')'      #function
 | '(' expr ')'                                                         #subexpression
 | K_CASE arg1=expr? ( K_WHEN expr K_THEN expr )+ ( K_ELSE expr )? K_END     #case_expression
 ;

table_constraint
 : ( K_CONSTRAINT name )?
   (
   K_CHECK '(' expr ')'
   )
 ;

ordering_term
 : expr ( K_ASC | K_DESC )?
 ;

result_column
 : '*'                              # result_column_star
 | table_name '.' '*'               # result_column_tableStar
 | expr ( K_AS? column_alias )?     # result_column_expr
 ;

table_or_subquery
 : ( database_name '.' )? table_name ( K_AS table_alias )?;

join_clause
 : ( join_operator table_or_subquery join_constraint )+
 ;

join_operator
 //: ','
 : K_HASH? ( (K_LEFT | K_RIGHT) K_OUTER? | K_INNER )? K_JOIN
 ;

join_constraint
 : ( K_ON expr )
//   | K_USING '(' column_name ( ',' column_name )* ')' )?
 ;

from_statement
 : K_FROM ( table_or_subquery join_clause? )
// : K_FROM ( table_or_subquery ( ',' table_or_subquery )* | join_clause )
;

where_operator
 : K_WHERE expr
 ;
 
group_by_operator
 : K_GROUP K_BY expr ( ',' expr )* having_operator?
 ;
 
having_operator
 : K_HAVING expr
 ;
 
signed_number
 : ( '+' | '-' )? NUMERIC_LITERAL
 ;

literal_value
 : NUMERIC_LITERAL
 | STRING_LITERAL
 | BLOB_LITERAL
 | K_NULL
 | K_CURRENT_TIME
 | K_CURRENT_DATE
 | K_CURRENT_TIMESTAMP
 ;

unary_operator
 : '-'
 | '+'
 | '~'
 | K_NOT
 ;

error_message
 : STRING_LITERAL
 ;

module_argument // TODO check what exactly is permitted here
 : expr
 | column_def
 ;

column_alias
 : IDENTIFIER
 | STRING_LITERAL
 ;

keyword
 : K_ADD
 | K_AFTER
 | K_ALL
 | K_ALTER
 | K_ANALYZE
 | K_AND
 | K_AS
 | K_ASC
 | K_ATTACH
 | K_BEFORE
 | K_BEGIN
 | K_BETWEEN
 | K_BY
 | K_CASCADE
 | K_CASE
 | K_CAST
 | K_CHECK
 | K_COLLATE
 | K_COLUMN
 | K_CONFLICT
 | K_CONSTRAINT
 | K_CREATE
 | K_CROSS
 | K_CURRENT_DATE
 | K_CURRENT_TIME
 | K_CURRENT_TIMESTAMP
 | K_DATABASE
 | K_DEFAULT
 | K_DEFERRABLE
 | K_DEFERRED
 | K_DESC
 | K_DETACH
 | K_DISTINCT
 | K_EACH
 | K_ELSE
 | K_END
 | K_ESCAPE
 | K_EXISTS
 | K_EXPLAIN
 | K_FAIL
 | K_FOR
 | K_FOREIGN
 | K_FROM
 | K_FULL
 | K_GLOB
 | K_GROUP
 | K_HAVING
 | K_IF
 | K_IGNORE
 | K_IN
 | K_INNER
 | K_INSERT
 | K_INSTEAD
 | K_INTERSECT
 | K_INTO
 | K_IS
 | K_ISNULL
 | K_JOIN
 | K_KEY
 | K_LEFT
 | K_LIKE
 | K_LIMIT
 | K_MATCH
 | K_NATURAL
 | K_NO
 | K_NOT
 | K_NOTNULL
 | K_NULL
 | K_OF
 | K_OFFSET
 | K_ON
 | K_OR
 | K_ORDER
 | K_OUTER
 | K_PRAGMA
 | K_PRIMARY
 | K_QUERY
 | K_QUOTE
 | K_RAISE
 | K_RECURSIVE
 | K_REFERENCES
 | K_REGEXP
 | K_REINDEX
 | K_RELEASE
 | K_RENAME
 | K_REMOVEHEADER
 | K_REPLACE
 | K_RESTRICT
 | K_RIGHT
 | K_ROW
 | K_SELECT
 | K_SET
 | K_TABLE
 | K_THEN
 | K_TO
 | K_UNION
 | K_UNIQUE
 | K_USING
 | K_VALUES
 | K_VIEW
 | K_WHEN
 | K_WHERE
 | K_WITH
 | K_WITHOUT
 ;

// TODO check all names below

name
 : any_name
 ;

function_name
 : any_name
 ;

database_name
 : any_name
 ;

table_name 
 : any_name
 ;

table_or_index_name 
 : any_name
 ;

new_table_name 
 : any_name
 ;

column_name 
 : any_name
 ;

collation_name 
 : any_name
 ;

foreign_table 
 : any_name
 ;

index_name 
 : any_name
 ;

trigger_name
 : any_name
 ;

view_name 
 : any_name
 ;

module_name 
 : any_name
 ;

pragma_name 
 : any_name
 ;

savepoint_name 
 : any_name
 ;

table_alias 
 : any_name
 ;

transaction_name
 : any_name
 ;

any_name
 : IDENTIFIER 
 | keyword
 | STRING_LITERAL
 | '(' any_name ')'
 ;

SCOL : ';';
DOT : '.';
OPEN_PAR : '(';
CLOSE_PAR : ')';
COMMA : ',';
ASSIGN : '=';
STAR : '*';
PLUS : '+';
MINUS : '-';
TILDE : '~';
PIPE2 : '||';
DIV : '/';
MOD : '%';
LT2 : '<<';
GT2 : '>>';
AMP : '&';
PIPE : '|';
LT : '<';
LT_EQ : '<=';
GT : '>';
GT_EQ : '>=';
EQ : '==';
NOT_EQ1 : '!=';
NOT_EQ2 : '<>';

// http://www.sqlite.org/lang_keywords.html
K_ADD : A D D;
K_AFTER : A F T E R;
K_ALL : A L L;
K_ALTER : A L T E R;
K_ANALYZE : A N A L Y Z E;
K_AND : A N D;
K_AS : A S;
K_ASC : A S C;
K_ATTACH : A T T A C H;
K_BEFORE : B E F O R E;
K_BEGIN : B E G I N;
K_BETWEEN : B E T W E E N;
K_BY : B Y;
K_CASCADE : C A S C A D E;
K_CASE : C A S E;
K_CAST : C A S T;
K_CHECK : C H E C K;
K_COLLATE : C O L L A T E;
K_COLUMN : C O L U M N;
K_COMMIT : C O M M I T;
K_CONFLICT : C O N F L I C T;
K_CONSTRAINT : C O N S T R A I N T;
K_CREATE : C R E A T E;
K_CROSS : C R O S S;
K_CURRENT_DATE : C U R R E N T '_' D A T E;
K_CURRENT_TIME : C U R R E N T '_' T I M E;
K_CURRENT_TIMESTAMP : C U R R E N T '_' T I M E S T A M P;
K_DATABASE : D A T A B A S E;
K_DEFAULT : D E F A U L T;
K_DEFERRABLE : D E F E R R A B L E;
K_DEFERRED : D E F E R R E D;
K_DELETE : D E L E T E;
K_DELIMITER : D E L I M I T E R;
K_DESC : D E S C;
K_DETACH : D E T A C H;
K_DISTINCT : D I S T I N C T;
K_EACH : E A C H;
K_ELSE : E L S E;
K_END : E N D;
K_ESCAPE : E S C A P E;
K_EXISTS : E X I S T S;
K_EXPLAIN : E X P L A I N;
K_FAIL : F A I L;
K_FOR : F O R;
K_FOREIGN : F O R E I G N;
K_FROM : F R O M;
K_FULL : F U L L;
K_GLOB : G L O B;
K_GROUP : G R O U P;
K_HAVING : H A V I N G;
K_HASH : H A S H;
K_IF : I F;
K_IGNORE : I G N O R E;
K_IN : I N;
K_INNER : I N N E R;
K_INSERT : I N S E R T;
K_INSTEAD : I N S T E A D;
K_INTERSECT : I N T E R S E C T;
K_INTO : I N T O;
K_IS : I S;
K_ISNULL : I S N U L L;
K_JOIN : J O I N;
K_KEY : K E Y;
K_LEFT : L E F T;
K_LIKE : L I K E;
K_LIMIT : L I M I T;
K_MATCH : M A T C H;
K_MAP : M A P;
K_NATURAL : N A T U R A L;
K_NO : N O;
K_NOT : N O T;
K_NOTNULL : N O T N U L L;
K_NULL : N U L L;
K_OF : O F;
K_OFFSET : O F F S E T;
K_ON : O N;
K_OR : O R;
K_ORDER : O R D E R;
K_OUTER : O U T E R;
K_PLAN : P L A N;
K_PRAGMA : P R A G M A;
K_PRIMARY : P R I M A R Y;
K_QUERY : Q U E R Y;
K_QUOTE : Q U O T E;
K_RAISE : R A I S E;
K_RECURSIVE : R E C U R S I V E;
K_REFERENCES : R E F E R E N C E S;
K_REGEXP : R E G E X P;
K_REINDEX : R E I N D E X;
K_RELEASE : R E L E A S E;
K_RENAME : R E N A M E;
K_REMOVEHEADER : R E M O V E H E A D E R;
K_REPLACE : R E P L A C E;
K_RESTRICT : R E S T R I C T;
K_RIGHT : R I G H T;
K_ROLLBACK : R O L L B A C K;
K_ROW : R O W;
K_SELECT : S E L E C T;
K_SET : S E T;
K_TABLE : T A B L E;
K_TEMP : T E M P;
K_THEN : T H E N;
K_TO : T O;
K_TYPE : T Y P E;
K_UNION : U N I O N;
K_UNIQUE : U N I Q U E;
K_UPDATE : U P D A T E;
K_USING : U S I N G;
K_VALUES : V A L U E S;
K_VIEW : V I E W;
K_VIRTUAL : V I R T U A L;
K_WHEN : W H E N;
K_WHERE : W H E R E;
K_WITH : W I T H;
K_WITHOUT : W I T H O U T;

IDENTIFIER
 : '"' (~'"' | '""')* '"'
 | '`' (~'`' | '``')* '`'
 | '[' ~']'* ']'
 | [a-zA-Z_] [a-zA-Z_0-9]* // TODO check: needs more chars in set
 ;

NUMERIC_LITERAL
 : DIGIT+ ( '.' DIGIT* )? ( E [-+]? DIGIT+ )?
 | '.' DIGIT+ ( E [-+]? DIGIT+ )?
 ;

BIND_PARAMETER
 : '?' DIGIT*
 | [:@$] IDENTIFIER
 ;

STRING_LITERAL
 : '\'' ( ~'\'' | '\'\'' )* '\''
 ;

BLOB_LITERAL
 : X STRING_LITERAL
 ;

SINGLE_LINE_COMMENT
 : '--' ~[\r\n]* -> channel(HIDDEN)
 ;

MULTILINE_COMMENT
 : '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN)
 ;

SPACES
 : [ \u000B\t\r\n] -> channel(HIDDEN)
 ;

UNEXPECTED_CHAR
 : .
 ;

fragment DIGIT : [0-9];

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];
