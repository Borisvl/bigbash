package de.zalando.bigbash.pipes;

import de.zalando.bigbash.entities.ProgramConfig;
import org.aeonbits.owner.ConfigCache;

/**
 * Created by boris on 10.07.14.
 */
public class BashGzFileInput implements BashInput {

    private final String filenames;

    public BashGzFileInput(final String filenames) {
        this.filenames = filenames;
    }

    @Override
    public String render() {
        return ConfigCache.getOrCreate(ProgramConfig.class).zcat() + " " + filenames;
    }
}
