package de.zalando.bigbash.parser;

import com.google.common.collect.ImmutableMap;
import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.CompressionType;
import de.zalando.bigbash.entities.FieldType;
import de.zalando.bigbash.entities.FileMappingProperties;
import de.zalando.bigbash.grammar.BashSqlLexer;
import de.zalando.bigbash.grammar.BashSqlListener;
import de.zalando.bigbash.grammar.BashSqlParser;
import de.zalando.bigbash.pipes.BashCommand;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by bvonloesch on 6/11/14.
 */
public class FromAndJoinTranslaterTest {

    private final String TEST_SELECT_STATEMENT1 =
        "SELECT table2.B, table1.sku from table1 join table2 on table2.appdomain=table1.appdomain where table1.sku=\"huhu\"";

    private final String TEST_SELECT_STATEMENT2 = "SELECT table2.B, table1.sku, table3.C "
            + "from table1 join table2 on table1.appdomain=table2.appdomain "
            + "join table3 on table1.appdomain=table3.appdomain " + "where table1.sku=\"huhu\"";

    private void walkStatement(final String statement, final BashSqlListener listener) throws IOException {
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
    public void testFromAndJoinTranslater() throws IOException {
        BashSqlTable table1 = new BashSqlTable();
        table1.setTableName("table1");
        table1.addColumn("table1", "sku", FieldType.TEXT, 0);
        table1.addColumn("table1", "appdomain", FieldType.INTEGER, 1);
        table1.addColumn("table1", "misc", FieldType.TEXT, 2);
        table1.setDelimiter(";");

        BashSqlTable table2 = new BashSqlTable();
        table2.setTableName("table2");
        table2.addColumn("table2", "appdomain", FieldType.INTEGER, 0);
        table2.addColumn("table2", "blub", FieldType.INTEGER, 1);
        table2.addColumn("table2", "B", FieldType.TEXT, 2);
        table2.setDelimiter(";");

        ImmutableMap<String, BashSqlTable> tableMap = ImmutableMap.of("table1", table1, "table2", table2);

        FileMappingProperties prop1 = new FileMappingProperties("*.gz", CompressionType.GZ, ";");
        FileMappingProperties prop2 = new FileMappingProperties("app.csv", CompressionType.NONE, ";");
        table1.setInput(new BashCommand(prop1.getPipeInput(";").render()));
        table2.setInput(new BashCommand(prop2.getPipeInput(";").render()));

        FromAndJoinTranslater translater = new FromAndJoinTranslater(tableMap);
        BashSqlSelectListener listener = new BashSqlSelectListener();
        walkStatement(TEST_SELECT_STATEMENT1, listener);
        System.out.println(translater.createJoinExpression(listener.getSelectStmtData().getFromStatementContext())
                .getInput().render());

    }
}
