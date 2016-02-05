package de.zalando.bigbash.parser;

import com.google.common.collect.Lists;
import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.ProgramConfig;
import de.zalando.bigbash.entities.SelectStmtData;
import de.zalando.bigbash.grammar.BashSqlBaseListener;
import de.zalando.bigbash.grammar.BashSqlListener;
import de.zalando.bigbash.grammar.BashSqlParser;
import org.aeonbits.owner.ConfigCache;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;

/**
 * Created by bvonloesch on 8/8/15.
 */
public class GroupBy2AwkParser {
    protected static final String COUNT = "COUNT";
    protected static final String SUM = "SUM";
    protected static final String MAX = "MAX";
    protected static final String MIN = "MIN";
    protected static final String GROUP_CONCAT = "GROUP_CONCAT";
    protected static final String DEFAULT_SEPERATOR = ",";
    protected final ProgramConfig programConfig;

    public GroupBy2AwkParser() {
        programConfig = ConfigCache.getOrCreate(ProgramConfig.class);
    }

    protected List<BashSqlParser.FunctionContext> getFunctionContexts(SelectStmtData stmt) {
        final List<BashSqlParser.FunctionContext> exprs = Lists.newLinkedList();
        BashSqlListener functionStatementCollector = new BashSqlBaseListener() {
            @Override
            public void enterFunction(@NotNull final BashSqlParser.FunctionContext ctx) {
                exprs.add(ctx);
            }
        };

        ParseTreeWalker walker = new ParseTreeWalker();

        // add all exprs that show up in the result columns
        for (BashSqlParser.Result_columnContext result : stmt.getReturnColumnsExpr()) {
            if (result instanceof BashSqlParser.Result_column_exprContext) {
                walker.walk(functionStatementCollector, ((BashSqlParser.Result_column_exprContext) result).expr());
            }
        }

        // add all exprs that show up in the having columns
        if (stmt.getHavingExpr() != null) {
            walker.walk(functionStatementCollector, stmt.getHavingExpr());
        }
        return exprs;
    }

    protected int[] extractColumnNumbersFromGroupStmt(final BashSqlTable stmtTable,
                                                      final List<BashSqlParser.ExprContext> groupByExpressions) {
        int[] sortColumnNumbers = new int[groupByExpressions.size()];
        int i = 0;

        // extract all the column numbers
        for (BashSqlParser.ExprContext expr : groupByExpressions) {
            sortColumnNumbers[i] = Integer.valueOf(new Expr2AwkTranslater2(stmtTable).translateSingleExprStmt(expr)
                    .replace("$", ""));
            i++;
        }

        return sortColumnNumbers;
    }
}
