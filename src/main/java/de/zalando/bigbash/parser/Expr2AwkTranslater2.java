package de.zalando.bigbash.parser;

import java.util.Map;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.escape.CharEscaperBuilder;
import com.google.common.escape.Escaper;

import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.grammar.BashSqlParser;

/**
 * Created by bvonloesch on 6/28/14.
 */
public class Expr2AwkTranslater2 implements ExprTranslater {

    private final BashSqlTable table;

    private final Map<String, String> map = ImmutableMap.<String, String>builder().put("AND", "&&").put("OR", "||")
                                                .put("<>", "!=").put("=", "==").build();
    private final Escaper escaper = new CharEscaperBuilder().addEscape('"', "\\\"").addEscape('\\', "\\\\").addEscape('\n', "\\n")
                                              .addEscape('\r', "\\r").addEscape('\t', "\\t").addEscape('\b', "\\b")
                                              .toEscaper();

    public Expr2AwkTranslater2(final BashSqlTable table) {
        this.table = table;
    }

    @Override
    public String translateSingleExprStmt(final BashSqlParser.ExprContext expr) {
        if (expr instanceof BashSqlParser.Boolean_expressionContext) {
            BashSqlParser.Boolean_expressionContext bexpr = (BashSqlParser.Boolean_expressionContext) expr;
            if (bexpr.op.getType() == BashSqlParser.K_REGEXP) {
                String regExp = translateSingleExprStmt(bexpr.arg2);
                if (regExp.startsWith("\"")) {
                    regExp = regExp.substring(1, regExp.length() - 1);
                }

                return translateSingleExprStmt(bexpr.arg1) + " ~ /" + regExp + "/";
            }

            return translateSingleExprStmt(bexpr.arg1) + " " + getAwkOperator(bexpr.op) + " "
                    + translateSingleExprStmt(bexpr.arg2);
        } else if (expr instanceof BashSqlParser.SubexpressionContext) {
            return "(" + translateSingleExprStmt((BashSqlParser.ExprContext) expr.getChild(1)) + ")";
        } else if (expr instanceof BashSqlParser.FunctionContext) {

            // First check whether we have already calculated expression
            Optional<Integer> columnNr = getColumnNr(expr);
            if (columnNr.isPresent()) {
                return handleColumnName(expr);
            }

            BashSqlParser.FunctionContext fExpr = (BashSqlParser.FunctionContext) expr;
            return fExpr.function_name().getText().toLowerCase() + "("
                    + Joiner.on(",").join(Iterables.transform(fExpr.expr(),
                            new Function<BashSqlParser.ExprContext, String>() {
                                @Override
                                public String apply(final BashSqlParser.ExprContext exprContext) {
                                    return translateSingleExprStmt(exprContext);
                                }
                            })) + ")";
        } else if (expr instanceof BashSqlParser.Column_name_defContext) {
            return handleColumnName(expr);
        } else if (expr instanceof BashSqlParser.SomethineContext) {
            BashSqlParser.SomethineContext sExpr = (BashSqlParser.SomethineContext) expr;
            if (sExpr.literal_value().STRING_LITERAL() != null) {
                return '"'
                        + escaper.escape(sExpr.literal_value().getText().substring(1,
                                sExpr.literal_value().getText().length() - 1)) + '"';
            } else {
                return sExpr.literal_value().getText();
            }
        }

        return null;
    }

    private String getAwkOperator(final Token op) {
        String ret = map.get(op.getText().toUpperCase());
        if (ret == null) {
            ret = op.getText();
        }

        return ret;
    }

    private Optional<Integer> getColumnNr(final BashSqlParser.ExprContext t) {
        String tableName = null;

        // check if there's a table name in front of the column
        if (t.getParent().getChildCount() == 3 && t.getParent().getChild(1).getText().equals(".")) {
            tableName = t.getParent().getChild(0).getText();
        }

        String columnName;
        if (tableName != null) {
            columnName = tableName + "." + t.getText();
        } else {
            columnName = t.getText();
        }

        if (table.getColumnInformation(columnName) == null) {
            return Optional.absent();
        } else {
            return Optional.of(table.getColumnInformation(columnName).getColumnNr() + 1);
        }
    }

    private String handleColumnName(final BashSqlParser.ExprContext t) {
        Optional<Integer> nr = getColumnNr(t);
        if (!nr.isPresent()) {
            throw new RuntimeException("Column name " + t.getText() + " does not exist in table schema "
                    + table.getTableName());
        }

        return "$" + nr.get().toString();
    }

}
