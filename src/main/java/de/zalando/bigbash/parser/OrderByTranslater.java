package de.zalando.bigbash.parser;

import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.FieldType;
import de.zalando.bigbash.entities.ProgramConfig;
import de.zalando.bigbash.grammar.BashSqlParser;
import org.aeonbits.owner.ConfigCache;

/**
 * Created by bvonloesch on 6/12/14.
 */
public class OrderByTranslater {

    private final BashSqlTable table;
    private final ProgramConfig programConfig;

    public OrderByTranslater(final BashSqlTable table) {
        this.table = table;
        programConfig = ConfigCache.getOrCreate(ProgramConfig.class);
    }

    public String createOrderByOutput(final BashSqlParser.Order_by_operatorContext ctx) {

        String sortParameter = "";
        for (BashSqlParser.Ordering_termContext ordering_ctx : ctx.ordering_term()) {
            String direction = "";
            if (ordering_ctx.K_DESC() != null) {
                direction = "r";
            }

            BashSqlTable.ColumnInformation column = findColumnName(ordering_ctx.expr());
            sortParameter += String.format(" -k %d,%d%s%s", column.getColumnNr() + 1, column.getColumnNr() + 1,
                    direction, column.getType() == FieldType.INTEGER ? "n" : "");
        }

        return String.format(programConfig.sort() + " -t$'%s' %s", table.getDelimiter(), sortParameter);
    }

    private BashSqlTable.ColumnInformation findColumnName(final BashSqlParser.ExprContext expr) {
        String columnName = expr.getText().toLowerCase();
        if (table.getColumnInformation(columnName) != null) {
            return table.getColumnInformation(columnName);
        }

        throw new RuntimeException("Only columns are allowed in order by expressions");
    }
}
