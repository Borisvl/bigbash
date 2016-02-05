package de.zalando.bigbash.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.grammar.BashSqlBaseListener;
import de.zalando.bigbash.grammar.BashSqlListener;
import de.zalando.bigbash.grammar.BashSqlParser;

/**
 * Created by boris on 07.07.14.
 */
public class SimpleWhereClauses {

    private final Map<String, BashSqlTable> tableMap;

    public SimpleWhereClauses(final Map<String, BashSqlTable> tableMap) {
        this.tableMap = tableMap;
    }

    public List<BashSqlParser.ExprContext> getSingleTableExpressions(final BashSqlParser.ExprContext whereExpr,
            final String tableName) {

        if (whereExpr == null) {
            return ImmutableList.of();
        }

        final Set<String> tables = Sets.newHashSet();
        List<BashSqlParser.ExprContext> result = Lists.newArrayList();

        BashSqlListener tableCollector = new BashSqlBaseListener() {
            @Override
            public void enterColumn_name_def(@NotNull final BashSqlParser.Column_name_defContext ctx) {
                BashSqlTable table = tableMap.get(tableName.toLowerCase());
                if (table.getColumnInformation(ctx.getText()) != null) {
                    tables.add(tableName);
                } else {
                    for (Map.Entry<String, BashSqlTable> entry : tableMap.entrySet()) {
                        if (entry.getValue().getColumnInformation(ctx.getText()) != null) {
                            tables.add(entry.getKey());
                        }
                    }
                }
            }
        };

        ParseTreeWalker walker = new ParseTreeWalker();

        if (whereExpr instanceof BashSqlParser.Boolean_expressionContext) {
            BashSqlParser.Boolean_expressionContext bWhereExpr = (BashSqlParser.Boolean_expressionContext) whereExpr;
            if (bWhereExpr.op.getType() == BashSqlParser.K_AND) {
                result.addAll(getSingleTableExpressions(bWhereExpr.arg1, tableName));
                result.addAll(getSingleTableExpressions(bWhereExpr.arg2, tableName));
            } else {
                tables.clear();
                walker.walk(tableCollector, whereExpr);
                if (tables.size() == 1 && tables.iterator().next().equals(tableName)) {
                    result.add(whereExpr);
                }
            }
        } else if (whereExpr instanceof BashSqlParser.SubexpressionContext) {
            result.addAll(getSingleTableExpressions(((BashSqlParser.SubexpressionContext) whereExpr).expr(),
                    tableName));
        } else {
            tables.clear();
            walker.walk(tableCollector, whereExpr);
            if (tables.size() == 1 && tables.iterator().next().equals(tableName)) {
                result.add(whereExpr);
            }
        }

        return result;
    }
}
