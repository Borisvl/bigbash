package de.zalando.bigbash.parser;

import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.FieldType;
import de.zalando.bigbash.entities.ProgramConfig;
import de.zalando.bigbash.grammar.BashSqlParser;
import de.zalando.bigbash.pipes.BashCommand;
import de.zalando.bigbash.pipes.BashPipe;
import org.aeonbits.owner.ConfigCache;

import java.util.List;

/**
 * Created by boris on 14.07.14.
 */
public class AddExpressionsTranslater {

    private final BashSqlTable table;

    public AddExpressionsTranslater(final BashSqlTable table) {
        this.table = table;
    }

    public BashSqlTable addExpressionsTranslator(final List<BashSqlParser.ExprContext> exprs) {
        if (exprs == null) {
            return table;
        }

        ExprTranslater translater = new Expr2AwkTranslater2(table);
        StringBuilder output = new StringBuilder("$0");
        boolean addColumns = false;
        for (BashSqlParser.ExprContext expr : exprs) {
            if (expr == null || table.getColumnInformation(expr.getText()) != null) {
                continue;
            }

            addColumns = true;
            if (output.length() > 0) {
                output.append(",");
            }

            String translatedExpr = translater.translateSingleExprStmt(expr);

            output.append(translater.translateSingleExprStmt(expr));

            // TODO: Integer is just a bad quess but should behave okay for order by in most cases
            table.addColumn(expr.getText(), FieldType.INTEGER, false, table.getColumnCount());
        }

        if (addColumns) {
            String awkOutput = String.format("%s 'BEGIN{FS=OFS=\"%s\"}{print %s}'",
                    ConfigCache.getOrCreate(ProgramConfig.class).awk(), table.getDelimiter(), output.toString());
            table.setInput(new BashPipe(table.getInput(), new BashCommand(awkOutput)));
        }

        return table;
    }
}
