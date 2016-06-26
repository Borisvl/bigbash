package de.zalando.bigbash.parser;

import com.google.common.collect.ImmutableMap;
import de.zalando.bigbash.entities.CompressionType;
import de.zalando.bigbash.entities.FileMappingProperties;
import de.zalando.bigbash.grammar.BashSqlLexer;
import de.zalando.bigbash.grammar.BashSqlParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BashSqlGeneralStmtListenerTest {

    private static final String TEST_CREATE_SQL_STMT =
        "CREATE TABLE testtable(n1 INTEGER, n2 REAL, t1 DATE, libertine);"
            + "CREATE TABLE anothertable(n1 INTEGER, n2 REAL, t1 DATE, libertine);"
            + "CREATE TABLE lasttable(n1 INTEGER, n2 REAL, t1 DATE, libertine);";

    private BashSqlGeneralStmtListener listenerUnderTest = null;

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
        FileMappingProperties prop1 = new FileMappingProperties("file1", CompressionType.NONE, ";");
        FileMappingProperties prop2 = new FileMappingProperties("file2", CompressionType.NONE, ";");
        FileMappingProperties prop3 = new FileMappingProperties("file3", CompressionType.NONE, ";");
        Map<String, FileMappingProperties> fileMappingPropertiesMap = ImmutableMap.of("testtable", prop1,
                "anothertable", prop2, "lasttable", prop3);
        listenerUnderTest = new BashSqlGeneralStmtListener(fileMappingPropertiesMap, false, ";");

        walker.walk(listenerUnderTest, tree);
    }

    @After
    public void tearDown() throws Exception { }

    @Test
    public void testTableSpaceStructure() {
        assertEquals(3, listenerUnderTest.createdTablespace.size());
    }

    @Test
    public void testTableSpaceNames() {
        assertTrue(listenerUnderTest.createdTablespace.containsKey("testtable"));
        assertTrue(listenerUnderTest.createdTablespace.containsKey("anothertable"));
        assertTrue(listenerUnderTest.createdTablespace.containsKey("lasttable"));
    }

}
