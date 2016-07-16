package de.zalando.bigbash.parser;

import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.EditPosition;
import de.zalando.bigbash.entities.FieldType;
import de.zalando.bigbash.exceptions.BigBashException;
import de.zalando.bigbash.grammar.BashSqlBaseListener;
import de.zalando.bigbash.grammar.BashSqlParser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

public class BashSqlCreateStmtListener extends BashSqlBaseListener {

    BashSqlTable createdTable = null;
    private int columnNr;

    @Override
    public void enterCreate_table_stmt(@NotNull final BashSqlParser.Create_table_stmtContext ctx) {
        createdTable = new BashSqlTable();
        columnNr = 0;
    }

    @Override
    public void exitCreate_table_stmt(@NotNull final BashSqlParser.Create_table_stmtContext ctx) {

        // perform some safety checks (despite grammar rules)
        if (createdTable.getTableName() == null || createdTable.getTableName().isEmpty()) {
            throw new BigBashException("A table must always have a valid name!",
                    EditPosition.fromContext(ctx.table_name()));
        }

        if (createdTable.getColumnCount() == 0) {
            throw new BigBashException("A table must always have at least one column!",
                    EditPosition.fromContext(ctx.table_name()));
        }
    }

    @Override
    public void exitTable_name(@NotNull final BashSqlParser.Table_nameContext ctx) {
        createdTable.setTableName(ctx.getChild(0).getText().toLowerCase());
    }

    @Override
    public void exitColumn_def(@NotNull final BashSqlParser.Column_defContext ctx) {
        FieldType type = FieldType.TEXT;
        boolean unique = false;
        if (ctx.getChildCount() > 1) {
            ParseTree desc = ctx.getChild(1);
            type = FieldType.fromString(desc.getChild(0).getText());
            if (desc.getChildCount() > 1 && "UNIQUE".equals(desc.getChild(1).getText().toUpperCase())) {
                unique = true;
            }
        }

        String columnName = ctx.getChild(0).getChild(0).getText();
        if (createdTable.getColumnInformation(columnName) != null) {
            throw new BigBashException("Column '" + columnName + "' already exists in table '" + createdTable.getTableName() + "'.",
                    EditPosition.fromContext(ctx));
        }
        createdTable.addColumn(createdTable.getTableName(), columnName, type, unique, columnNr);
        columnNr++;
    }

    public BashSqlTable getResultTable() {
        return this.createdTable;
    }

}
