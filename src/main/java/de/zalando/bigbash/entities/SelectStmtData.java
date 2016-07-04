package de.zalando.bigbash.entities;

import com.google.common.collect.Lists;
import de.zalando.bigbash.grammar.BashSqlParser;

import java.util.List;

/**
 * Created by bvonloesch on 6/10/14.
 */
public class SelectStmtData {

    private BashSqlParser.ExprContext whereExpr;
    private List<BashSqlParser.ExprContext> groupByExpr;
    private BashSqlParser.ExprContext havingExpr;
    private BashSqlParser.From_statementContext fromStatementContext;
    private List<BashSqlParser.Result_columnContext> returnColumnsExpr;
    private BashSqlParser.Order_by_operatorContext orderByContext;

    private String tableName;
    private Integer limit;
    private Integer offset;
    private BashSqlParser.Select_stmtContext selectStmt;

    public void addReturnColumn(final BashSqlParser.Result_columnContext exprContext) {
        if (returnColumnsExpr == null) {
            returnColumnsExpr = Lists.newArrayList();
        }

        returnColumnsExpr.add(exprContext);
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public List<BashSqlParser.Result_columnContext> getReturnColumnsExpr() {
        return returnColumnsExpr;
    }

    public BashSqlParser.ExprContext getWhereExpr() {
        return whereExpr;
    }

    public void setWhereExpr(final BashSqlParser.ExprContext whereExpr) {
        this.whereExpr = whereExpr;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(final Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(final Integer offset) {
        this.offset = offset;
    }

    public List<BashSqlParser.ExprContext> getGroupByExpr() {
        return groupByExpr;
    }

    public void setGroupByExpr(final List<BashSqlParser.ExprContext> groupByExpr) {
        this.groupByExpr = groupByExpr;
    }

    public BashSqlParser.ExprContext getHavingExpr() {
        return havingExpr;
    }

    public void setHavingExpr(final BashSqlParser.ExprContext havingExpr) {
        this.havingExpr = havingExpr;
    }

    public void setReturnColumnsExpr(final List<BashSqlParser.Result_columnContext> returnColumnsExpr) {
        this.returnColumnsExpr = returnColumnsExpr;
    }

    public BashSqlParser.From_statementContext getFromStatementContext() {
        return fromStatementContext;
    }

    public void setFromStatementContext(final BashSqlParser.From_statementContext fromStatementContext) {
        this.fromStatementContext = fromStatementContext;
    }

    public BashSqlParser.Order_by_operatorContext getOrderByContext() {
        return orderByContext;
    }

    public void setOrderByContext(final BashSqlParser.Order_by_operatorContext orderByContext) {
        this.orderByContext = orderByContext;
    }

    public void setSelectStmt(final BashSqlParser.Select_stmtContext selectStmt) {
        this.selectStmt = selectStmt;
    }

    public BashSqlParser.Select_stmtContext getSelectStmt() {
        return selectStmt;
    }

    public boolean isDistinctQuery() {
        return selectStmt.K_DISTINCT() != null;
    }
}
