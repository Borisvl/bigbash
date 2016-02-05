package de.zalando.bigbash.commandline;

import com.google.common.collect.Lists;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.List;

/**
 * Created by bvonloesch on 7/24/15.
 */
public class Arguments {

    @Option(name = "--noAnsiC", usage = "Use this if you have problems with the default Ansi-C decoding")
    private boolean useDefaultEnc = false;

    @Option(name = "--sortAggregation", usage = "Uses sort instead of hash based aggregation")
    private boolean useSortBasedAggregation = false;

    @Option(name = "-f", aliases = {"--file"}, usage = "SQL file that should be processed")
    private List<File> sqlFiles = Lists.newArrayList();
    ;

    @Option(name = "-d", usage = "Output delimiter")
    private String outputSeparator = "\\t";

    @Option(name = "--help", usage = "Print this message")
    private boolean help = false;

    @Argument(usage = "SQL commands")
    private List<String> sqls = Lists.newArrayList();

    public boolean isUseDefaultEnc() {
        return useDefaultEnc;
    }

    public List<File> getSqlFiles() {
        return sqlFiles;
    }

    public String getOutputSeparator() {
        return outputSeparator;
    }

    public boolean isHelp() {
        return help;
    }

    public List<String> getSqls() {
        return sqls;
    }

    public boolean isUseSortBasedAggregation() {
        return useSortBasedAggregation;
    }
}
