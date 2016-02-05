package de.zalando.bigbash.pipes;

import de.zalando.bigbash.entities.ProgramConfig;
import org.aeonbits.owner.ConfigCache;

/**
 * Created by boris on 10.07.14.
 */
public class BashFileInput implements BashInput {

    private final String fileNames;

    public BashFileInput(final String fileNames) {
        this.fileNames = fileNames;
    }

    @Override
    public String render() {
        return ConfigCache.getOrCreate(ProgramConfig.class).cat() + " " + fileNames;
    }
}
