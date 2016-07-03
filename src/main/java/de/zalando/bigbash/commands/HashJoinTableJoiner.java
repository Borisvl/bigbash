package de.zalando.bigbash.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.JoinType;
import de.zalando.bigbash.entities.ProgramConfig;
import de.zalando.bigbash.pipes.BashCommand;
import org.aeonbits.owner.ConfigCache;
import org.stringtemplate.v4.ST;

import java.util.List;

/**
 * Created by bvonloesch on 6/21/14.
 */
public class HashJoinTableJoiner implements TableJoiner {

    private final ProgramConfig programConfig;

    public HashJoinTableJoiner() {
        programConfig = ConfigCache.getOrCreate(ProgramConfig.class);
    }

    @Override
    public BashSqlTable join(final BashSqlTable table1, final BashSqlTable table2, final String columnName1,
            final String columnName2, final JoinType joinType) {
        Preconditions.checkArgument(columnName1.indexOf('.') > 0);
        Preconditions.checkArgument(columnName2.indexOf('.') > 0);

        if (joinType == JoinType.RIGHT || joinType == JoinType.OUTER) {
            throw new IllegalArgumentException("Only inner and left hash joins are supported. Use a normal join instead.");
        }

        BashSqlTable newTable = new BashSqlTable();
        newTable.setDelimiter(table1.getDelimiter());


        int columnNr = 0;

        List<Integer> table1Output = Lists.newArrayList();
        final boolean column2Unique = table2.getColumnInformation(columnName2).isUnique();
        for (int i = 0; i < table1.getColumnCount(); i++) {
            String columnName = table1.getColumnNameFromColumnNr(i);

            table1Output.add(i);

            BashSqlTable.ColumnInformation columnInfo = table1.getColumnInformation(columnName);

            //Column is guaranteed to stay unique if joined field is also unique
            boolean stayUnique = column2Unique && columnInfo.isUnique();
            newTable.addColumn(columnName, columnInfo.getType(), stayUnique, columnNr++);
        }

        List<Integer> table2Output = Lists.newArrayList();
        final boolean column1Unique = table1.getColumnInformation(columnName1).isUnique();
        for (int i = 0; i < table2.getColumnCount(); i++) {
            String columnName = table2.getColumnNameFromColumnNr(i);

            table2Output.add(i);

            BashSqlTable.ColumnInformation columnInfo = table2.getColumnInformation(columnName);

            //Column is guaranteed to stay unique if joined field is also unique
            boolean stayUnique = column1Unique && columnInfo.isUnique();
            newTable.addColumn(columnName, columnInfo.getType(), stayUnique, columnNr++);
        }

        String delimiter = table1.getDelimiter();
        int columnNr1 = table1.getColumnInformation(columnName1).getColumnNr();
        int columnNr2 = table2.getColumnInformation(columnName2).getColumnNr();

        ST output = new ST(
                "<awk> 'BEGIN{FS=OFS=\"<del>\"}NR==FNR{m[$<joinColumn2>]++; map[$<joinColumn2>,m[$<joinColumn2>]-1]=<table2Output>; next}"
                    + "{c=m[$<joinColumn1>]; if (c) for (i=0;i\\<c;i++) print <table1Output>,map[$<joinColumn1>,i]; "
                    + "<if(leftJoin)>else print <table1Output>,<nulls>;<endif>}' \\<(<inputTable2>) \\<(<inputTable1>)");

        if (table2.getColumnInformation(columnName2).isUnique()) {
            output = new ST("<awk> 'BEGIN{FS=OFS=\"<del>\"}NR==FNR{map[$<joinColumn2>]=<table2Output>; next}"
                    + "{c=map[$<joinColumn1>]; if (c) print <table1Output>,c; <if(leftJoin)>else print <table1Output>,<nulls>;<endif>}' "
                    + "\\<(<inputTable2>) \\<(<inputTable1>)");
        }

        output.add("awk", programConfig.awk());
        output.add("del", delimiter);
        output.add("joinColumn2", columnNr2 + 1);
        output.add("joinColumn1", columnNr1 + 1);
        output.add("table1Output", "$0");
        output.add("table2Output", "$0");
        output.add("leftJoin", joinType == JoinType.LEFT);
        output.add("inputTable1", table1.getInput().render());
        output.add("inputTable2", table2.getInput().render());

        String nulls = "";
        for (Integer aTable2Output : table2Output) {
            if (nulls.length() > 0) {
                nulls += delimiter;
            }

            nulls += "";
        }

        nulls = "\"" + nulls + "\"";
        output.add("nulls", nulls);

        newTable.setInput(new BashCommand(output.render()));
        return newTable;
    }

}
