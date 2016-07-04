package de.zalando.bigbash.parser;

import de.zalando.bigbash.entities.ProgramConfig;
import org.aeonbits.owner.ConfigCache;

/**
 * Created by bvonloesch on 04/07/16.
 */
public class DistinctTranslater {
    private final ProgramConfig programConfig;

    public DistinctTranslater() {
        programConfig = ConfigCache.getOrCreate(ProgramConfig.class);
    }

    public String createDistinctOutput() {
        return programConfig.sort() + " -u";
    }

}
