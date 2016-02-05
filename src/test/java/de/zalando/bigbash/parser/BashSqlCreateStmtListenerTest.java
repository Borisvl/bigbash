package de.zalando.bigbash.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.zalando.bigbash.entities.FieldType;
import de.zalando.bigbash.grammar.BashSqlLexer;
import de.zalando.bigbash.grammar.BashSqlParser;

public class BashSqlCreateStmtListenerTest {

    private static final String TEST_CREATE_SQL_STMT =
        "CREATE TABLE testtable(n1 INTEGER, n2 REAL, t1 DATE, libertine);";

    private BashSqlCreateStmtListener listenerUnderTest = null;

    @Before
    public void setUp() throws Exception {
        InputStream stream = new ByteArrayInputStream(TEST_CREATE_SQL_STMT.getBytes(StandardCharsets.UTF_8));
        ANTLRInputStream input = new ANTLRInputStream(stream);   // create a lexer that feeds off of input CharStream
        BashSqlLexer lexer = new BashSqlLexer(input);            // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer); // create a parser that feeds off the tokens buffer
        BashSqlParser parser = new BashSqlParser(tokens);
        ParseTree tree = parser.parse();                         // begin parsing at init rule

        ParseTreeWalker walker = new ParseTreeWalker();

        // Walk the tree created during the parse, trigger callbacks
        listenerUnderTest = new BashSqlCreateStmtListener();

        walker.walk(listenerUnderTest, tree);
    }

    @After
    public void tearDown() throws Exception { }

    @Test
    public void testTableNameAndStructure() {
        assertNotNull(listenerUnderTest.createdTable);
        assertEquals("testtable", listenerUnderTest.createdTable.getTableName());
        assertEquals(4, listenerUnderTest.createdTable.getColumnCount());
    }

    @Test
    public void testTableColumnNames() {
        assertTrue(listenerUnderTest.createdTable.getColumns().containsKey("testtable.n1"));
        assertTrue(listenerUnderTest.createdTable.getColumns().containsKey("testtable.n2"));
        assertTrue(listenerUnderTest.createdTable.getColumns().containsKey("testtable.t1"));
        assertTrue(listenerUnderTest.createdTable.getColumns().containsKey("testtable.libertine"));
    }

    @Test
    public void testTableColumnTypes() {
        assertEquals(FieldType.INTEGER, listenerUnderTest.createdTable.getColumnInformation("n1").getType());
        assertEquals(FieldType.REAL, listenerUnderTest.createdTable.getColumnInformation("n2").getType());
        assertEquals(FieldType.DATE, listenerUnderTest.createdTable.getColumnInformation("t1").getType());
        assertEquals(FieldType.TEXT, listenerUnderTest.createdTable.getColumnInformation("libertine").getType());
    }

}
