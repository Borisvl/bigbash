package de.zalando.bigbash.parser;

import org.antlr.v4.runtime.misc.NotNull;

import de.zalando.bigbash.entities.SelectStmtData;
import de.zalando.bigbash.grammar.BashSqlBaseListener;
import de.zalando.bigbash.grammar.BashSqlParser;

/**
 * Created by bvonloesch on 6/10/14.
 */
public class BashSqlSelectListener extends BashSqlBaseListener {

    private boolean inSelectStatement = false;
    private boolean inResultColumn;

    private final SelectStmtData data;

    public BashSqlSelectListener() {
        data = new SelectStmtData();
    }

    @Override
    public void enterSelect_stmt(@NotNull final BashSqlParser.Select_stmtContext ctx) {
        data.setSelectStmt(ctx);
        data.setTableName(ctx.from_statement().table_or_subquery().table_name().getText());
        data.setFromStatementContext(ctx.from_statement());

        for (BashSqlParser.Result_columnContext resultColumn : ctx.result_column()) {
            data.addReturnColumn(resultColumn);
        }

        if (ctx.where_operator() != null) {
            data.setWhereExpr(ctx.where_operator().expr());
        }

        if (ctx.limit_operator() != null) {
            data.setLimit(Integer.parseInt(ctx.limit_operator().expr(0).getText()));
            if (ctx.limit_operator().K_OFFSET() != null) {
                data.setOffset(Integer.parseInt(ctx.limit_operator().expr(1).getText()));
            }
        }

        if (ctx.group_by_operator() != null) {
            data.setGroupByExpr(ctx.group_by_operator().expr());

            if (ctx.group_by_operator().having_operator() != null) {
                data.setHavingExpr(ctx.group_by_operator().having_operator().expr());
            }
        }

        if (ctx.order_by_operator() != null) {
            data.setOrderByContext(ctx.order_by_operator());
        }

    }

    public SelectStmtData getSelectStmtData() {
        return data;
    }
}
