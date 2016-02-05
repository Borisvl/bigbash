package de.zalando.bigbash.commandline;

import de.zalando.bigbash.entities.FileMappingProperties;
import de.zalando.bigbash.entities.ProgramConfig;
import de.zalando.bigbash.grammar.BashSqlLexer;
import de.zalando.bigbash.grammar.BashSqlParser;
import de.zalando.bigbash.parser.BashSqlGeneralStmtListener;
import org.aeonbits.owner.ConfigCache;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class BashCompiler {

    private final ProgramConfig programConfig;

    public BashCompiler() {
        programConfig = ConfigCache.getOrCreate(ProgramConfig.class);
    }

    public String compile(final String sql, final Map<String, FileMappingProperties> fileMappingPropertiesMap,
                          boolean useSortAggregation)
        throws IOException {
        InputStream stream = new ByteArrayInputStream(sql.getBytes());
        ANTLRInputStream input = new ANTLRInputStream(stream);   // create a lexer that feeds off of input CharStream
        BashSqlLexer lexer = new BashSqlLexer(input);            // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer); // create a parser that feeds off the tokens buffer
        BashSqlParser parser = new BashSqlParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());

        ParseTree tree = parser.parse(); // begin parsing at init rule

        ParseTreeWalker walker = new ParseTreeWalker();

        // Walk the tree created during the parse, trigger callbacks
        BashSqlGeneralStmtListener listener = new BashSqlGeneralStmtListener(fileMappingPropertiesMap, useSortAggregation);
        walker.walk(listener, tree);
        return listener.getOutput();
    }
}
