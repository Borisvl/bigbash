package de.zalando.bigbash.parser;

import com.google.common.collect.ImmutableMap;
import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.FieldType;
import de.zalando.bigbash.grammar.BashSqlLexer;
import de.zalando.bigbash.grammar.BashSqlParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * Created by boris on 07.07.14.
 */
public class SimpleWhereClausesTest {

    public void walkStatement(final String statement, final BashSqlSelectListener listener) throws IOException {
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
    public void testSingleStatement() throws IOException {

        BashSqlSelectListener listenerUnderTest = new BashSqlSelectListener();
        BashSqlTable aTable = new BashSqlTable();
        aTable.addColumn("b", FieldType.INTEGER, false, 0);
        aTable.addColumn("c", FieldType.INTEGER, false, 1);

        walkStatement("Select * from A where b=5", listenerUnderTest);

        SimpleWhereClauses simpleWhereClauses = new SimpleWhereClauses(ImmutableMap.of("a", aTable));
        assertEquals(1,
            simpleWhereClauses.getSingleTableExpressions(listenerUnderTest.getSelectStmtData().getWhereExpr(), "A")
                    .size());
        walkStatement("Select * from A where b=5 AND c=1", listenerUnderTest);
        simpleWhereClauses = new SimpleWhereClauses(ImmutableMap.of("a", aTable));
        assertEquals(2,
            simpleWhereClauses.getSingleTableExpressions(listenerUnderTest.getSelectStmtData().getWhereExpr(), "A")
                    .size());
    }

    @Test
    public void testJoinStatement() throws IOException {
        BashSqlSelectListener listenerUnderTest = new BashSqlSelectListener();
        BashSqlTable aTable = new BashSqlTable();
        aTable.addColumn("ab", FieldType.INTEGER, false, 0);
        aTable.addColumn("ac", FieldType.INTEGER, false, 1);

        BashSqlTable bTable = new BashSqlTable();
        bTable.addColumn("bb", FieldType.INTEGER, false, 0);
        bTable.addColumn("bc", FieldType.INTEGER, false, 1);

        walkStatement("Select * from A join B on A.ab = B.bb where ac=5 OR bc=6", listenerUnderTest);

        SimpleWhereClauses simpleWhereClauses = new SimpleWhereClauses(ImmutableMap.of("a", aTable, "b", bTable));
        assertEquals(0,
            simpleWhereClauses.getSingleTableExpressions(listenerUnderTest.getSelectStmtData().getWhereExpr(), "A")
                    .size());
        assertEquals(0,
            simpleWhereClauses.getSingleTableExpressions(listenerUnderTest.getSelectStmtData().getWhereExpr(), "B")
                    .size());

        walkStatement("Select * from A join B on A.ab = B.bb where ab=5 AND (bc=6 OR (bb=1 AND ac=1))",
            listenerUnderTest);
        simpleWhereClauses = new SimpleWhereClauses(ImmutableMap.of("a", aTable, "b", bTable));
        assertEquals(1,
            simpleWhereClauses.getSingleTableExpressions(listenerUnderTest.getSelectStmtData().getWhereExpr(), "A")
                    .size());
        assertEquals(0,
            simpleWhereClauses.getSingleTableExpressions(listenerUnderTest.getSelectStmtData().getWhereExpr(), "B")
                    .size());

        walkStatement("Select * from A join B on A.ab = B.bb where ab=bc+1 AND substr(bb,0,1)==bb", listenerUnderTest);
        simpleWhereClauses = new SimpleWhereClauses(ImmutableMap.of("a", aTable, "b", bTable));
        assertEquals(1,
            simpleWhereClauses.getSingleTableExpressions(listenerUnderTest.getSelectStmtData().getWhereExpr(), "B")
                    .size());
        assertEquals(0,
            simpleWhereClauses.getSingleTableExpressions(listenerUnderTest.getSelectStmtData().getWhereExpr(), "A")
                    .size());

    }
}
