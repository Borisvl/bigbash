package de.zalando.bigbash.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.zalando.bigbash.commands.BashJoinTableJoiner;
import de.zalando.bigbash.commands.TableJoiner;
import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.CompressionType;
import de.zalando.bigbash.entities.FieldType;
import de.zalando.bigbash.entities.FileMappingProperties;
import de.zalando.bigbash.entities.JoinType;
import de.zalando.bigbash.pipes.BashCommand;

/**
 * Created by bvonloesch on 6/11/14.
 */
public class BashJoinTableJoinerTest {

    @Test
    public void test() {
        BashSqlTable table1 = new BashSqlTable();
        FileMappingProperties prop1 = new FileMappingProperties("*.gz", CompressionType.GZ, ";");
        FileMappingProperties prop2 = new FileMappingProperties("../brands.csv", CompressionType.NONE, ";");

        String table1Name = "table1";
        table1.setTableName(table1Name);
        table1.addColumn(table1Name, "A", FieldType.TEXT, 0);
        table1.addColumn(table1Name, "B", FieldType.TEXT, 1);
        table1.addColumn(table1Name, "C", FieldType.TEXT, 2);
        table1.setDelimiter(";");
        table1.setInput(new BashCommand(prop1.getPipeInput().render()));

        BashSqlTable table2 = new BashSqlTable();
        String table2Name = "table2";
        table2.setTableName(table2Name);
        table2.addColumn(table2Name, "B", FieldType.TEXT, 0);
        table2.addColumn(table2Name, "D", FieldType.TEXT, 1);
        table2.addColumn(table2Name, "E", FieldType.TEXT, 2);
        table2.setDelimiter(";");
        table2.setInput(new BashCommand(prop2.getPipeInput().render()));

        TableJoiner joiner = new BashJoinTableJoiner();
        BashSqlTable joinedTable = joiner.join(table1, table2, "table1.B", "table2.B", JoinType.INNER);

        assertEquals(6, joinedTable.getColumnCount());
        assertEquals("table1.a", joinedTable.getColumnNameFromColumnNr(0));
        assertEquals("table1.b", joinedTable.getColumnNameFromColumnNr(1));
        assertEquals("table1.c", joinedTable.getColumnNameFromColumnNr(2));
        assertEquals("table2.b", joinedTable.getColumnNameFromColumnNr(3));
        assertEquals("table2.d", joinedTable.getColumnNameFromColumnNr(4));
        assertEquals("table2.e", joinedTable.getColumnNameFromColumnNr(5));

        System.out.println(joinedTable.getInput().render());
    }

}
