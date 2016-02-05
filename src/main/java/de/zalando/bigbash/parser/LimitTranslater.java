package de.zalando.bigbash.parser;

import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.ProgramConfig;
import org.aeonbits.owner.ConfigCache;

/**
 * Created by bvonloesch on 6/12/14.
 */
public class LimitTranslater {
    private final BashSqlTable table;
    private final ProgramConfig programConfig;

    public LimitTranslater(final BashSqlTable table) {
        this.table = table;
        programConfig = ConfigCache.getOrCreate(ProgramConfig.class);
    }

    public String createLimitOutput(final Integer limit, final Integer offset) {
        if (offset == null) {
            return String.format("%s -n%d", programConfig.head(), limit);
        } else {
            return String.format("%s -n '%d,%dp'", programConfig.sed(), offset, offset + limit - 1);
        }
    }

}
