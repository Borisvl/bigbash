package de.zalando.bigbash.service;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import de.zalando.bigbash.entities.EditPosition;
import de.zalando.bigbash.entities.FileMappingProperties;
import de.zalando.bigbash.exceptions.BigBashException;
import de.zalando.bigbash.grammar.BashSqlLexer;
import de.zalando.bigbash.grammar.BashSqlParser;
import de.zalando.bigbash.parser.BashSqlGeneralStmtListener;
import de.zalando.bigbash.util.CollectingErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by bvonloesch on 10/06/16.
 */
public class SqlToBashConverter {

    public static class ConversionResult {
        String script;
        List<CollectingErrorListener.SyntaxError> errors;
        boolean success;

        public ConversionResult(String script, List<CollectingErrorListener.SyntaxError> errors) {
            this.script = script;
            this.errors = errors;
            success = errors == null || errors.isEmpty();
        }

        public String getScript() {
            return script;
        }

        public List<CollectingErrorListener.SyntaxError> getErrors() {
            return errors;
        }

        public boolean isSuccess() {
            return success;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("script", script)
                    .add("errors", errors)
                    .toString();
        }
    }

    public ConversionResult getBashScript(InputStream sqlStream, String outputDelimiter, boolean sortBasedAggregation, boolean noAnsiC) throws IOException {
        String prePut = "(trap \"kill 0\" SIGINT; ";
        if (!noAnsiC) {
            prePut += "export LC_ALL=C; ";
        }
        String postPut = ")";
        //FileMappingProperties.outputDelimiter = outputDelimiter;
        Map<String, FileMappingProperties> fileMappingPropertiesMap = Maps.newHashMap();

        ANTLRInputStream input = new ANTLRInputStream(sqlStream);
        BashSqlLexer lexer = new BashSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BashSqlParser parser = new BashSqlParser(tokens);
        CollectingErrorListener errorListener = new CollectingErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ParseTree tree = parser.parse(); // begin parsing at init rule
        if (!errorListener.getErrors().isEmpty()) {
            return new ConversionResult(null, errorListener.getErrors());
        }
        ParseTreeWalker walker = new ParseTreeWalker();
        try {
            // Walk the tree created during the parse, trigger callbacks
            BashSqlGeneralStmtListener listener = new BashSqlGeneralStmtListener(fileMappingPropertiesMap,
                    sortBasedAggregation, outputDelimiter);
            walker.walk(listener, tree);
            return new ConversionResult(prePut + listener.getOutput() + postPut, null);
        } catch (BigBashException ex) {
            return new ConversionResult(null, ImmutableList.of(
                    new CollectingErrorListener.SyntaxError(ex.getPosition(), ex.getMessage())));
        } catch (RuntimeException ex) {
            //Fallback
            return new ConversionResult(null, ImmutableList.of(
                    new CollectingErrorListener.SyntaxError(new EditPosition(1, 1, 1, 1), ex.getMessage())));
        }
    }

}
