package de.zalando.bigbash.commands;

import com.google.common.base.Preconditions;
import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.JoinType;
import de.zalando.bigbash.entities.ProgramConfig;
import de.zalando.bigbash.pipes.BashCommand;
import de.zalando.bigbash.pipes.BashInput;
import de.zalando.bigbash.pipes.BashPipe;
import org.aeonbits.owner.ConfigCache;

/**
 * Created by bvonloesch on 6/11/14.
 */
public class BashJoinTableJoiner implements TableJoiner {

    private final ProgramConfig programConfig;

    public BashJoinTableJoiner() {
        programConfig = ConfigCache.getOrCreate(ProgramConfig.class);
    }

    @Override
    public BashSqlTable join(final BashSqlTable table1, final BashSqlTable table2, final String columnName1,
            final String columnName2, final JoinType joinType) {
        Preconditions.checkArgument(columnName1.indexOf('.') > 0);
        Preconditions.checkArgument(columnName2.indexOf('.') > 0);

        if (!table1.getDelimiter().equals(table2.getDelimiter())) {
            throw new RuntimeException("Joined tables must have the same delimiter");
        }

        String lowerCaseColumnName1 = columnName1.toLowerCase();
        String lowerCaseColumnName2 = columnName2.toLowerCase();

        BashSqlTable newTable = new BashSqlTable();
        newTable.setDelimiter(table1.getDelimiter());

        int columnNr = 0;
        String columnsForJoinCommand = " ";
        for (int i = 0; i < table1.getColumnCount(); i++) {
            String columnName = table1.getColumnNameFromColumnNr(i);

            BashSqlTable.ColumnInformation columnInfo = table1.getColumnInformation(columnName);

            //Column is guaranteed to stay unique if joined field is also unique
            boolean stayUnique = table2.getColumnInformation(columnName2).isUnique() && columnInfo.isUnique();
            newTable.addColumn(columnName, columnInfo.getType(), stayUnique, columnNr++);

            columnsForJoinCommand += "1." + (i + 1) + " ";
        }

        for (int i = 0; i < table2.getColumnCount(); i++) {
            String columnName = table2.getColumnNameFromColumnNr(i);

            BashSqlTable.ColumnInformation columnInfo = table2.getColumnInformation(columnName);

            //Column is guaranteed to stay unique if joined field is also unique
            boolean stayUnique = table1.getColumnInformation(columnName1).isUnique() && columnInfo.isUnique();
            newTable.addColumn(columnName, columnInfo.getType(), stayUnique, columnNr++);

            columnsForJoinCommand += "2." + (i + 1) + " ";
        }

        String joinOperator = getStringFromJoinType(joinType);

        BashPipe p = new BashPipe();
        p.addInput(getSortedInputPipe(table1, lowerCaseColumnName1));
        p.addInput(getSortedInputPipe(table2, lowerCaseColumnName2));

        int columnNr1 = table1.getColumnInformation(lowerCaseColumnName1).getColumnNr();
        int columnNr2 = table2.getColumnInformation(lowerCaseColumnName2).getColumnNr();
        p.setOutput(new BashCommand(
                String.format(programConfig.join() + " %s -e '' -o '%s' -t $'%s' -1 %d -2 %d", joinOperator,
                    columnsForJoinCommand, table1.getDelimiter(), columnNr1 + 1, columnNr2 + 1)));
        newTable.setInput(p);

        return newTable;
    }

    private String getStringFromJoinType(final JoinType joinType) {
        String joinOperator = "";
        if (joinType == JoinType.LEFT) {
            joinOperator = "-a1";
        } else if (joinType == JoinType.RIGHT) {
            joinOperator = "-a2";
        } else if (joinType == JoinType.OUTER) {
            joinOperator = "-a1 -a2";
        }

        return joinOperator;
    }

    private BashInput getSortedInputPipe(final BashSqlTable table, final String columnName) {
        int columnNr = table.getColumnInformation(columnName).getColumnNr();
        return new BashPipe(table.getInput(),
                new BashCommand(
                        String.format("%s -t $'%s' -k %d,%d", programConfig.sort(), table.getDelimiter(), columnNr + 1,
                        columnNr + 1)));
    }
}
