package de.zalando.bigbash.commands;

import com.google.common.collect.Lists;
import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.ProgramConfig;
import de.zalando.bigbash.pipes.BashCommand;
import de.zalando.bigbash.pipes.BashPipe;
import org.aeonbits.owner.ConfigCache;
import org.stringtemplate.v4.ST;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by bvonloesch on 7/31/14.
 */
public class TableStripper {

    private final ProgramConfig programConfig;

    public TableStripper() {
        programConfig = ConfigCache.getOrCreate(ProgramConfig.class);
    }

    public BashSqlTable stripTable(final BashSqlTable input, final Collection<BashSqlTable.ColumnInformation> columns) {
        List<Integer> columnNrs = Lists.newArrayList();
        for (BashSqlTable.ColumnInformation column : columns) {

            // +1 for cut command
            columnNrs.add(column.getColumnNr() + 1);
        }

        // Do not strip anything if all columns are used
        if (columnNrs.size() >= input.getColumnCount()) {
            return input;
        }

        Collections.sort(columnNrs);

        BashSqlTable strippedDownTable = new BashSqlTable();
        strippedDownTable.setTableName(input.getTableName());
        strippedDownTable.setDelimiter(input.getDelimiter());
        for (int columnNr : columnNrs) {
            strippedDownTable.addColumn(input.getColumnNameFromColumnNr(columnNr - 1),
                    input.getColumnInformation(columnNr - 1).getType(), input.getColumnInformation(columnNr - 1).isUnique(),
                    strippedDownTable.getColumnCount());
        }

        ST output = new ST("<cut> -d $'<delimiter>' -f<columnNr;separator=\",\">");
        output.add("cut", programConfig.cut());
        output.add("delimiter", input.getDelimiter());
        output.add("columnNr", columnNrs);
        strippedDownTable.setInput(new BashPipe(input.getInput(), new BashCommand(output.render())));
        return strippedDownTable;
    }
}
