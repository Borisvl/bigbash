package de.zalando.bigbash.parser;

import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.FieldType;
import de.zalando.bigbash.entities.SelectStmtData;
import de.zalando.bigbash.grammar.BashSqlParser;
import de.zalando.bigbash.grammar.BashSqlParser.ExprContext;

import java.util.List;

public class HashedGroupBy2AwkParser extends GroupBy2AwkParser {

    public HashedGroupBy2AwkParser() {
        super();
    }

    public String parseGroupByStmt(final SelectStmtData stmt, final BashSqlTable table) {

        // check for handling of group statements
        if (stmt.getGroupByExpr() != null && !stmt.getGroupByExpr().isEmpty()) {

            String[] funcAdditions = getFunctionExpressionPart(stmt, table);

            String groupByBeginStmt = buildGroupingCountVarInitString(table);
            String groupByIncStmt = buildGroupingContinueCountStmt(table, stmt.getGroupByExpr(), funcAdditions[0]);
            String groupByResetStmt = buildGroupingEndCountStmt(funcAdditions[1]);

            return programConfig.awk() + " -F '" + table.getDelimiter() + "' '"
                    + groupByBeginStmt + " " + groupByIncStmt + " " + groupByResetStmt + "'";
        } else {
            return new SortedGroupBy2AwkParser().parseGroupByStmt(stmt, table);
        }
    }

    private String[] getFunctionExpressionPart(final SelectStmtData stmt, final BashSqlTable table) {
        ExprTranslater expr2AwkTranslater = new Expr2AwkTranslater2(table);
        String incStmt = "";
        String printStmt = "";

        int i = 0;

        final List<BashSqlParser.FunctionContext> exprs = getFunctionContexts(stmt);

        for (BashSqlParser.FunctionContext expr : exprs) {
            String functionName = expr.function_name().getText();
            boolean isDistinct = false;
            int functionColumnNr = 2;
            if (expr.K_DISTINCT() != null) {
                functionColumnNr = 3;
                isDistinct = true;
            }

            String functionString = expr.getText();

            if (table.getColumnInformation(functionString) != null) {

                // Function is already in our table, do not recalculate
                continue;
            }

            if (COUNT.equals(functionName.toUpperCase())) {
                String tempName = functionName + "_" + i;
                if (isDistinct) {
                    String transExpr = "*";
                    if (expr.expr().size() > 0) {
                        transExpr = expr2AwkTranslater.translateSingleExprStmt(expr.expr(0));
                    }

                    String aggrTempName = "ag_" + tempName;
                    incStmt += String.format("if (!(k %s in %s)) %s[k] += 1; %s[k %s]=1;", transExpr, aggrTempName, tempName, aggrTempName, transExpr);
                    printStmt += "," + tempName + "[k]";
                } else {
                    incStmt += tempName + "[k] +=1;";
                    printStmt += "," + tempName + "[k]";
                }
            } else if (functionName.toUpperCase().equals(SUM)) {
                String transExpr = expr2AwkTranslater.translateSingleExprStmt(expr.expr(0));
                String tempName = functionName + "_" + i;
                incStmt += tempName + "[k] +=" + transExpr + ";";
                printStmt += "," + tempName + "[k]";

            } else if (functionName.toUpperCase().equals(GROUP_CONCAT)) {
                String groupConcatSeparator = DEFAULT_SEPERATOR;
                if (expr.expr().size() > 1) {
                    groupConcatSeparator = expr.getChild(functionColumnNr + 2).getText();
                }

                String transExpr = expr2AwkTranslater.translateSingleExprStmt(expr.expr(0));
                String tempName = functionName + "_" + i;
                if (isDistinct) {
                    String aggrTempName = "ag_" + tempName;
                    incStmt += String.format("if (!(k %s in %s)) %s[k] = %s[k] %s \"%s\"; %s[k %s]=1;", transExpr, aggrTempName, tempName, tempName, transExpr, groupConcatSeparator, aggrTempName, transExpr);
                    printStmt += ","
                            + String.format("substr(%s[k], 1, length(%s[k])-1)", tempName, tempName);
                    printStmt += "," + tempName + "[k]";
                } else {

                    incStmt += String.format("%s[k]=%s[k] %s \"%s\";", tempName, tempName, transExpr, groupConcatSeparator);
                    printStmt += ","
                            + String.format("substr(%s[k], 1, length(%s[k])-1)", tempName, tempName);
                }
            } else if (functionName.toUpperCase().equals(MAX)) {
                String transExpr = expr2AwkTranslater.translateSingleExprStmt(expr.expr(0));
                String tempName = functionName + "_" + i;
                incStmt += "if(!(k in " + tempName + ")||" + transExpr + ">" + tempName + "[k])" + tempName + "[k]=" + transExpr + ";";
                printStmt += "," + tempName + "[k]";
            } else if (functionName.toUpperCase().equals(MIN)) {
                String tempName = functionName + "_" + i;
                String transExpr = expr2AwkTranslater.translateSingleExprStmt(expr.expr(0));
                incStmt += "if(!(k in " + tempName + ")||" + transExpr + "<" + tempName + "[k])" + tempName + "[k]=" + transExpr + ";";
                printStmt += "," + tempName + "[k]";
            } else {
                continue;
                //throw new RuntimeException("Unknown function '" + functionName + "'");
            }

            table.addColumn(functionString, FieldType.INTEGER, false, table.getColumnCount());

            i++;
        }

        return new String[]{incStmt, printStmt};
    }

    private String buildGroupingCountVarInitString(final BashSqlTable stmtTable) {
        return "BEGIN {OFS=\"" + stmtTable.getDelimiter() + "\"}";
    }

    private String buildGroupingContinueCountStmt(final BashSqlTable stmtTable,
                                                  final List<ExprContext> groupByExpressions, final String funcAdditions) {

        int[] sortColumnNumbers = extractColumnNumbersFromGroupStmt(stmtTable, groupByExpressions);
        StringBuilder b = new StringBuilder();
        for (int sortColumnNumber : sortColumnNumbers) {
            b.append("$" + sortColumnNumber + "\"::\"");
        }
        b.delete(b.length() - 4, b.length());

        return "{k=" + b.toString() + "; row[k]=$0;" + funcAdditions + "}";
    }

    private String buildGroupingEndCountStmt(final String funcAdditions) {
        return "END{for (k in row) print row[k]" + funcAdditions + "}";
    }

}
