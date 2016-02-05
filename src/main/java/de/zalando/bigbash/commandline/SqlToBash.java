package de.zalando.bigbash.commandline;

import com.google.common.collect.Maps;
import de.zalando.bigbash.entities.FileMappingProperties;
import de.zalando.bigbash.grammar.BashSqlLexer;
import de.zalando.bigbash.grammar.BashSqlParser;
import de.zalando.bigbash.parser.BashSqlGeneralStmtListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.input.ReaderInputStream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.util.Map;

/**
 * Created by bvonloesch on 6/12/14.
 */
public class SqlToBash {

    public static final String USAGE = "java -jar sql2bash.jar [options...] VAL...";

    public static void main(final String[] args) throws IOException {
        Arguments arguments = new Arguments();
        CmdLineParser cmdLineParser = new CmdLineParser(arguments);

        try {
            cmdLineParser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println(USAGE);
            cmdLineParser.printUsage(System.err);
            return;
        }

        if (arguments.isHelp()) {
            System.err.println(USAGE);
            cmdLineParser.printUsage(System.err);
            return;
        }
        if (arguments.getSqlFiles().size() == 0 && arguments.getSqls().size() == 0) {
            System.err.println(USAGE);
            cmdLineParser.printUsage(System.err);
            return;
        }

        String prePut = "(trap \"kill 0\" SIGINT; ";
        if (!arguments.isUseDefaultEnc()) {
            prePut += "export LC_ALL=C; ";
        }
        String postPut = ")";
        FileMappingProperties.outputDelimiter = arguments.getOutputSeparator();
        Map<String, FileMappingProperties> fileMappingPropertiesMap = Maps.newHashMap();
        InputStream stream = new ReaderInputStream(new StringReader(""));
        for (int i = 0; i < arguments.getSqlFiles().size(); i++) {
            stream = new SequenceInputStream(stream, new FileInputStream(arguments.getSqlFiles().get(i)));
        }
        for (int i = 0; i < arguments.getSqls().size(); i++) {
            stream = new SequenceInputStream(stream,
                    new ReaderInputStream(new StringReader(arguments.getSqls().get(0) + ";")));
        }
        ANTLRInputStream input = new ANTLRInputStream(stream);
        BashSqlLexer lexer = new BashSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BashSqlParser parser = new BashSqlParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());

        ParseTree tree = parser.parse(); // begin parsing at init rule

        ParseTreeWalker walker = new ParseTreeWalker();

        // Walk the tree created during the parse, trigger callbacks
        BashSqlGeneralStmtListener listener = new BashSqlGeneralStmtListener(fileMappingPropertiesMap,
                arguments.isUseSortBasedAggregation());
        walker.walk(listener, tree);
        System.out.println(prePut + listener.getOutput() + postPut);
    }

}
