package de.zalando.bigbash.commandline;

import de.zalando.bigbash.service.SqlToBashConverter;
import org.apache.commons.io.input.ReaderInputStream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.*;

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
        InputStream stream = new ReaderInputStream(new StringReader(""));
        for (int i = 0; i < arguments.getSqlFiles().size(); i++) {
            stream = new SequenceInputStream(stream, new FileInputStream(arguments.getSqlFiles().get(i)));
        }
        for (int i = 0; i < arguments.getSqls().size(); i++) {
            stream = new SequenceInputStream(stream,
                    new ReaderInputStream(new StringReader(arguments.getSqls().get(0) + ";")));
        }

        SqlToBashConverter converter = new SqlToBashConverter();
        SqlToBashConverter.ConversionResult result = converter.getBashScript(stream, arguments.getOutputSeparator(),
                arguments.isUseSortBasedAggregation(), !arguments.isUseDefaultEnc());
        if (result.isSuccess()) {
            System.out.println(result.getScript());
        } else {
            System.err.println(result.getErrors().get(0).toString());
        }
    }

}
