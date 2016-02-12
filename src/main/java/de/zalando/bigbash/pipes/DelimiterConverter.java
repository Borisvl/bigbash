package de.zalando.bigbash.pipes;

import de.zalando.bigbash.entities.ProgramConfig;
import org.aeonbits.owner.ConfigCache;

/**
 * Created by bvonloesch on 7/22/15.
 */
public class DelimiterConverter implements BashInput {

    public static final String TAB_CHAR = "$(printf '\\t')";
    private final String delimiter;
    private final String replacement;

    public DelimiterConverter(String delimiter, String replacement) {
        this.delimiter = delimiter;
        this.replacement = replacement;
    }


    @Override
    public String render() {
        if ((delimiter.length() == 1 || delimiter.length() == 2 && delimiter.charAt(0) =='\\')
                && (replacement.length() == 1 || replacement.length() == 2 && replacement.charAt(0) =='\\')) {
            return ConfigCache.getOrCreate(ProgramConfig.class).tr() + " $'" + delimiter + "'" + " $'" + replacement + "'";
        }

        //This strange replacement is necessary since sed is really old on Mac OS X and does not support \t
        String localDelimiter = delimiter.replace("\\t", TAB_CHAR);
        String localReplacement = replacement.replace("\\t", TAB_CHAR);

        return ConfigCache.getOrCreate(ProgramConfig.class).sed() +
                String.format(" \"s/%s/%s/g\"", localDelimiter, localReplacement);
    }
}
