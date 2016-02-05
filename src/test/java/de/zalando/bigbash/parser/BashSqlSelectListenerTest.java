package de.zalando.bigbash.parser;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.junit.Test;

import de.zalando.bigbash.entities.SelectStmtData;
import de.zalando.bigbash.grammar.BashSqlLexer;
import de.zalando.bigbash.grammar.BashSqlParser;

/**
 * Created by bvonloesch on 6/10/14.
 */
public class BashSqlSelectListenerTest {

    private final String TEST_SELECT_STATEMENT1 = "SELECT appdomain, sku from table1 where sku=\"huhu\"";

    private void walkStatement(final String statement, final BashSqlSelectListener listener) throws IOException {
        InputStream stream = new ByteArrayInputStream(statement.getBytes(StandardCharsets.UTF_8));
        ANTLRInputStream input = new ANTLRInputStream(stream);   // create a lexer that feeds off of input CharStream
        BashSqlLexer lexer = new BashSqlLexer(input);            // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer); // create a parser that feeds off the tokens buffer
        BashSqlParser parser = new BashSqlParser(tokens);
        BashSqlParser.ParseContext tree = parser.parse();        // begin parsing at init rule

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, tree);
    }

    @Test
    public void test() throws IOException {

        // Walk the tree created during the parse, trigger callbacks
        BashSqlSelectListener listenerUnderTest = new BashSqlSelectListener();

        walkStatement(TEST_SELECT_STATEMENT1, listenerUnderTest);

        assertEquals("table1", listenerUnderTest.getSelectStmtData().getTableName());
        assertEquals(2, listenerUnderTest.getSelectStmtData().getReturnColumnsExpr().size());
        assertEquals("appdomain", listenerUnderTest.getSelectStmtData().getReturnColumnsExpr().get(0).getText());
        assertEquals("sku", listenerUnderTest.getSelectStmtData().getReturnColumnsExpr().get(1).getText());
    }

    @Test
    public void testLimit() throws IOException {

        // Walk the tree created during the parse, trigger callbacks
        BashSqlSelectListener listenerUnderTest = new BashSqlSelectListener();

        walkStatement("Select * from table1 LIMIT 10", listenerUnderTest);

        SelectStmtData data = listenerUnderTest.getSelectStmtData();
        assertEquals("table1", data.getTableName());
        assertEquals(1, data.getReturnColumnsExpr().size());
        assertEquals(10, data.getLimit().intValue());
        assertEquals(null, data.getOffset());
    }

    @Test
    public void testLimitAndOffset() throws IOException {

        // Walk the tree created during the parse, trigger callbacks
        BashSqlSelectListener listenerUnderTest = new BashSqlSelectListener();

        walkStatement("Select * from table1 LIMIT 10 OFFSET 100", listenerUnderTest);

        SelectStmtData data = listenerUnderTest.getSelectStmtData();
        assertEquals("table1", data.getTableName());
        assertEquals(1, data.getReturnColumnsExpr().size());
        assertEquals(10, data.getLimit().intValue());
        assertEquals(100, data.getOffset().intValue());
    }

}
