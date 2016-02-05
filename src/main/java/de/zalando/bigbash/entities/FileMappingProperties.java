package de.zalando.bigbash.entities;

import com.google.common.base.Optional;
import de.zalando.bigbash.pipes.BashCommand;
import de.zalando.bigbash.pipes.BashFileInput;
import de.zalando.bigbash.pipes.BashGzFileInput;
import de.zalando.bigbash.pipes.BashInput;
import de.zalando.bigbash.pipes.BashPipe;
import de.zalando.bigbash.pipes.BashRemoveHeaderInput;
import de.zalando.bigbash.pipes.DelimiterAndQuoteConverter;
import de.zalando.bigbash.pipes.DelimiterConverter;
import org.aeonbits.owner.ConfigCache;

/**
 * Created by bvonloesch on 6/11/14.
 */
public class FileMappingProperties {
    public static String outputDelimiter = "\\t";
    private final String files;
    private final CompressionType compressionType;
    private final String delimiter;
    private final boolean removeHeader;
    private final Optional<Character> quoteChar;
    //private String outputDelimiter;

    public FileMappingProperties(final String files, final CompressionType compressionType, final String delimiter) {
        this(files, compressionType, delimiter, false, Optional.<Character>absent());
    }

    public FileMappingProperties(final String files, final CompressionType compressionType, final String delimiter, 
                                 boolean removeHeader, Optional<Character> quoteChar) {
        this.files = files;
        this.compressionType = compressionType;
        this.delimiter = delimiter;
        this.removeHeader = removeHeader;
        this.quoteChar = quoteChar;
    }

    // public String getFiles() {
    // return files;
    // }

    public String getDelimiter() {
        return outputDelimiter;
    }

    public BashInput getPipeInput() {
        ProgramConfig programConfig = ConfigCache.getOrCreate(ProgramConfig.class);
        
        if (compressionType == CompressionType.GZ) {
            BashInput input = new BashGzFileInput(files);
            if (removeHeader) {
                input = new BashRemoveHeaderInput(files, new BashGzFileInput("{}"));                
            }
            if (quoteChar.isPresent()) {
                input = new BashPipe(input, new DelimiterAndQuoteConverter(delimiter, outputDelimiter, quoteChar.get()));
            }
            else if (!delimiter.equals(outputDelimiter)) {
                input = new BashPipe(input, new DelimiterConverter(delimiter, outputDelimiter));
            }
            return input;
        } else if (compressionType == CompressionType.NONE) {
            BashInput input = new BashFileInput(files);
            if (removeHeader) {
                input = new BashRemoveHeaderInput(files, new BashFileInput("{}"));
            }
            if (quoteChar.isPresent()) {
                input = new BashPipe(input, new DelimiterAndQuoteConverter(delimiter, outputDelimiter, quoteChar.get()));
            }
            else if (!delimiter.equals(outputDelimiter)) {
                input = new BashPipe(input, new DelimiterConverter(delimiter, outputDelimiter));
            }
            return input;
        } else if (compressionType == CompressionType.SQLITE3) {
            String[] databaseInfos = files.split(":");
            return new BashCommand(programConfig.sqlite3()
                    + String.format(" -csv -separator $'%s' %s \"Select * from %s\"", outputDelimiter, databaseInfos[0],
                    databaseInfos[1]));
        } else if (compressionType == CompressionType.RAW) {
            if (removeHeader) {
                throw new RuntimeException("Cannot remove header in RAW mapping.");
            }
            String command = files;
            if (command.startsWith("'")) {
                command = command.substring(1, command.length() - 1);
            }

            BashInput input = new BashCommand(command);
            if (quoteChar.isPresent()) {
                input = new BashPipe(input, new DelimiterAndQuoteConverter(delimiter, outputDelimiter, quoteChar.get()));
            }
            else if (!delimiter.equals(outputDelimiter)) {
                input = new BashPipe(input, new DelimiterConverter(delimiter, outputDelimiter));
            }
            return input;
        }

        throw new RuntimeException("Unknown file format '" + compressionType.toString() + "'");
    }
}
