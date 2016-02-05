package de.zalando.bigbash.parser;

import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.FieldType;
import de.zalando.bigbash.grammar.BashSqlLexer;
import de.zalando.bigbash.grammar.BashSqlParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SortedGroupBy2AwkParserTest {

    private final String TEST_SELECT_STATEMENT1 =
        "Select sum(price), count(*) from realtimelogging where price <> '-' Group By appdomain having sum(temp) > 100;";

// private final String TEST_SELECT_STATEMENT1 = "SELECT action,appdomain from table1 GROUP BY action,appdomain";

// private final String TEST_SELECT_STATEMENT1 = "SELECT action,appdomain,count(*) from table1 GROUP BY
// action,appdomain";

    private BashSqlSelectListener listener = null;

    @Before
    public void setUp() throws Exception {
        InputStream stream = new ByteArrayInputStream(TEST_SELECT_STATEMENT1.getBytes(StandardCharsets.UTF_8));
        ANTLRInputStream input = new ANTLRInputStream(stream);   // create a lexer that feeds off of input CharStream
        BashSqlLexer lexer = new BashSqlLexer(input);            // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer); // create a parser that feeds off the tokens buffer
        BashSqlParser parser = new BashSqlParser(tokens);
        ParseTree tree = parser.parse();                         // begin parsing at init rule

        ParseTreeWalker walker = new ParseTreeWalker();

// // Walk the tree created during the parse, trigger callbacks
        listener = new BashSqlSelectListener();

        walker.walk(listener, tree);

        System.out.println(tree.toStringTree(parser));
    }

    @Test
    public void test() {
        BashSqlTable t1 = new BashSqlTable();
        t1.setTableName("table1");

        t1.addColumn("table1", "actiondate", FieldType.DATE, 0);
        t1.addColumn("table1", "appdomain", FieldType.INTEGER, 1);
        t1.addColumn("table1", "sku", FieldType.TEXT, 2);
        t1.addColumn("table1", "simplesku", FieldType.TEXT, 3);
        t1.addColumn("table1", "customerId", FieldType.TEXT, 4);
        t1.addColumn("table1", "sessionId", FieldType.TEXT, 5);
        t1.addColumn("table1", "action", FieldType.TEXT, 6);
        t1.addColumn("table1", "salecount", FieldType.INTEGER, 7);
        t1.addColumn("table1", "price", FieldType.INTEGER, 8);
        t1.addColumn("table1", "orderId", FieldType.TEXT, 9);
        t1.addColumn("table1", "cookieId", FieldType.TEXT, 10);
        t1.addColumn("table1", "shobabtest", FieldType.TEXT, 11);
        t1.addColumn("table1", "internalReferer", FieldType.INTEGER, 12);

        Map<String, BashSqlTable> tables = new HashMap<String, BashSqlTable>();
        tables.put("table1", t1);

// String result = new Select2AwkParser(tables).parse(listener.getSelectStmtData());
// System.out.println(result);
    }

}
