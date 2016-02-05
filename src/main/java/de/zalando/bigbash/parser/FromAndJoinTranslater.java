package de.zalando.bigbash.parser;

import java.util.Map;

import de.zalando.bigbash.commands.BashJoinTableJoiner;
import de.zalando.bigbash.commands.HashJoinTableJoiner;
import de.zalando.bigbash.commands.TableJoiner;
import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.JoinType;
import de.zalando.bigbash.grammar.BashSqlParser;

/**
 * Created by bvonloesch on 6/11/14.
 */
public class FromAndJoinTranslater {

    public static class TableAndOutput {
        final String output;
        final BashSqlTable table;
        final String delimiter;

        public TableAndOutput(final String output, final BashSqlTable table, final String delimiter) {
            this.output = output;
            this.table = table;
            this.delimiter = delimiter;
        }

        public String getOutput() {
            return output;
        }

        public BashSqlTable getTable() {
            return table;
        }

        public String getDelimiter() {
            return delimiter;
        }
    }

    private final Map<String, BashSqlTable> tables;

    public FromAndJoinTranslater(final Map<String, BashSqlTable> tables) {
        this.tables = tables;
    }

    public BashSqlTable createJoinExpression(final BashSqlParser.From_statementContext ctx) {
        String table1 = ctx.table_or_subquery().table_name().getText();
        BashSqlTable bashSqltable1 = tables.get(table1.toLowerCase());

        if (ctx.join_clause() == null) {

            return bashSqltable1;
        } else {
            int nrOfJoins = ctx.join_clause().table_or_subquery().size();
            for (int i = 0; i < nrOfJoins; i++) {
                String newTable = ctx.join_clause().table_or_subquery(i).getText();
                BashSqlParser.Join_operatorContext joinOperator = ctx.join_clause().join_operator(i);
                JoinType jointype = getJoinType(joinOperator);

                boolean hashJoin = false;
                if (ctx.join_clause().join_operator(i).K_HASH() != null) {
                    hashJoin = true;
                }

                BashSqlTable bashSqlNewTable = tables.get(newTable.toLowerCase());

                BashSqlParser.ExprContext joinExpr = ctx.join_clause().join_constraint(i).expr();

                if (!("=".equals(joinExpr.getChild(1).getText()) || "==".equals(joinExpr.getChild(1).getText()))) {
                    throw new RuntimeException("Only equal operator is allowed in join on expression.");
                }

                BashSqlParser.ExprContext rightSide = (BashSqlParser.ExprContext) joinExpr.getChild(0);
                if (rightSide.children.size() != 3) {
                    throw new RuntimeException("You must use tablename.columnname in join expressions");
                }

                if (!rightSide.getChild(0).getClass().equals(BashSqlParser.Table_nameContext.class)) {
                    throw new RuntimeException("You must use tablename.columnname in join expressions");
                }

                if (!rightSide.getChild(2).getClass().equals(BashSqlParser.Column_nameContext.class)) {
                    throw new RuntimeException("You must use tablename.columnname in join expressions");
                }

                String column1 = joinExpr.getChild(0).getChild(0).getText() + "."
                        + joinExpr.getChild(0).getChild(2).getText();

                String column2 = joinExpr.getChild(2).getChild(0).getText() + "."
                        + joinExpr.getChild(2).getChild(2).getText();

                if (bashSqltable1.getColumnInformation(column1) == null) {
                    String buf = column1;
                    column1 = column2;
                    column2 = buf;
                }

                if (bashSqltable1.getColumnInformation(column1) == null) {
                    throw new RuntimeException("Could not find column " + column1 + " in table.");
                }

                TableJoiner joiner = new BashJoinTableJoiner();
                if (hashJoin) {
                    joiner = new HashJoinTableJoiner();
                }

                bashSqltable1 = joiner.join(bashSqltable1, bashSqlNewTable, column1, column2, jointype);

            }

            return bashSqltable1;
        }

    }

    public static JoinType getJoinType(final BashSqlParser.Join_operatorContext joinOperator) {
        JoinType jointype = JoinType.INNER;
        if (joinOperator.K_INNER() == null) {
            if (joinOperator.K_LEFT() != null) {
                jointype = JoinType.LEFT;
            } else if (joinOperator.K_RIGHT() != null) {
                jointype = JoinType.RIGHT;
            } else if (joinOperator.K_OUTER() != null) {
                jointype = JoinType.OUTER;
            }
        }

        return jointype;
    }
}
