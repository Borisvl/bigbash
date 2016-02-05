package de.zalando.bigbash.pipes;

import de.zalando.bigbash.entities.ProgramConfig;
import org.aeonbits.owner.ConfigCache;

/**
 * Created by bvonloesch on 7/22/15.
 */
public class DelimiterConverter implements BashInput {

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
        return ConfigCache.getOrCreate(ProgramConfig.class).sed() + 
                String.format(" 's/%s/%s/g'", delimiter, replacement);
    }
}
