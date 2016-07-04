package de.zalando.bigbash.parser;

import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.ProgramConfig;
import de.zalando.bigbash.grammar.BashSqlParser;
import org.aeonbits.owner.ConfigCache;

import java.util.List;

/**
 * Created by bvonloesch on 6/12/14.
 */
public class ReturnColumnsTranslater {

    private final BashSqlTable table;
    private final ExprTranslater expr2AwkTranslater;

    public ReturnColumnsTranslater(final BashSqlTable table) {
        this.table = table;
        expr2AwkTranslater = new Expr2AwkTranslater2(table);
    }

    public String translateReturnColumns(final List<BashSqlParser.Result_columnContext> returnColumnsExpr) {
        StringBuilder printStatement = new StringBuilder();
        for (BashSqlParser.Result_columnContext expr : returnColumnsExpr) {
            if (printStatement.length() > 0) {
                printStatement.append("\"" + table.getDelimiter() + "\"");
            }
            if (expr instanceof BashSqlParser.Result_column_starContext) {
                printStatement.append("$0");
            } else if (table.getColumnInformation(expr.getText()) != null) {
                printStatement.append("$" + (table.getColumnInformation(expr.getText()).getColumnNr() + 1));
            } else if (expr instanceof BashSqlParser.Result_column_exprContext) {
                printStatement.append(expr2AwkTranslater.translateSingleExprStmt(
                        ((BashSqlParser.Result_column_exprContext) expr).expr()));
            } else {
                BashSqlParser.Result_column_tableStarContext tableStar = (BashSqlParser.Result_column_tableStarContext)
                        expr;
                String tableName = tableStar.table_name().getText().toLowerCase();
                for (String columnNames : table.getColumns().keySet()) {
                    if (columnNames.startsWith(tableName + ".")) {
                        if (printStatement.length() > 0) {
                            printStatement.append("\"" + table.getDelimiter() + "\"");
                        }

                        printStatement.append("$" + (table.getColumnInformation(columnNames).getColumnNr() + 1));
                    }
                }
            }
        }

        return String.format(ConfigCache.getOrCreate(ProgramConfig.class).awk() + " -F '%s' '{print %s}'",
                table.getDelimiter(), printStatement.toString());
    }
}
