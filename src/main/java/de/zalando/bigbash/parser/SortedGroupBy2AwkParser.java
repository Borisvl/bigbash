package de.zalando.bigbash.parser;

import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.FieldType;
import de.zalando.bigbash.entities.SelectStmtData;
import de.zalando.bigbash.grammar.BashSqlParser;
import de.zalando.bigbash.grammar.BashSqlParser.ExprContext;
import org.stringtemplate.v4.ST;

import java.util.List;

public class SortedGroupBy2AwkParser extends GroupBy2AwkParser {

    public SortedGroupBy2AwkParser() {
        super();
    }

    public String parseGroupByStmt(final SelectStmtData stmt, final BashSqlTable table) {

        // check for handling of group statements
        if (stmt.getGroupByExpr() != null && !stmt.getGroupByExpr().isEmpty()) {
            String groupBySortStmt = buildSortStatementFromMultipleGroupByColumns(table, stmt.getGroupByExpr(),
                    table.getDelimiter());

            String[] funcAdditions = getFunctionExpressionPart(stmt, table);

            String printStmt = "if(lastrow!=0){" + funcAdditions[3] + " print lastrow" + funcAdditions[4] + "}";

            String groupByBeginStmt = buildGroupingCountVarInitString(table, stmt.getGroupByExpr(), funcAdditions[0]);
            String groupByIncStmt = buildGroupingContinueCountStmt(table, stmt.getGroupByExpr(), funcAdditions[1]);
            String groupByResetStmt = buildGroupingEndCountStmt(table, stmt.getGroupByExpr(), printStmt,
                    funcAdditions[2]);

            return groupBySortStmt + " | " + programConfig.awk() + " -F '" + table.getDelimiter() + "' '"
                    + groupByBeginStmt + " " + groupByIncStmt + " " + groupByResetStmt + " END {" + printStmt + "}'"; // <- group by + count
        } else {                                                                                                      // check if it contains
            // only plain functions as
            String[] funcAdditions = getFunctionExpressionPart(stmt, table);
            if (!funcAdditions[4].isEmpty()) { // apparently there's something there

                String printStmt = funcAdditions[3] + " print $0" + funcAdditions[4];

                String groupByBeginStmt = "BEGIN {" + funcAdditions[0] + "}";
                String groupByIncStmt = "{" + funcAdditions[1] + "}";

                return programConfig.awk() + " -F '" + table.getDelimiter() + "' '" + groupByBeginStmt + " "
                        + groupByIncStmt + " END {" + printStmt + "}'"; // <- group by + count
            }
        }

        return null;
    }

    private String[] getFunctionExpressionPart(final SelectStmtData stmt, final BashSqlTable table) {
        ExprTranslater expr2AwkTranslater = new Expr2AwkTranslater2(table);
        String beginStmtInit = "";
        String incStmt = "";
        String resetStmt = "";
        String printStmt = "";
        String prepPrintStmt = "";

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
                    incStmt += String.format("%s[%s]=1;", tempName, transExpr);
                    resetStmt += String.format("delete %s; %s[%s]=1;", tempName, tempName, transExpr);
                    prepPrintStmt += String.format("%s=0; for (n in %s) %s++;", aggrTempName, tempName, aggrTempName);
                    printStmt += "\"" + table.getDelimiter() + "\"" + aggrTempName;
                } else {
                    beginStmtInit += tempName + "=0;";
                    incStmt += tempName + "+=1;";
                    resetStmt += tempName + "=1;";
                    printStmt += "\"" + table.getDelimiter() + "\"" + tempName;
                }
            } else if (functionName.toUpperCase().equals(SUM)) {
                String transExpr = expr2AwkTranslater.translateSingleExprStmt(expr.expr(0));
                String tempName = functionName + "_" + i;
                beginStmtInit += tempName + "=0;";
                incStmt += tempName + "+=" + transExpr + ";";
                resetStmt += tempName + "=" + transExpr + ";";
                printStmt += "\"" + table.getDelimiter() + "\"" + tempName;

            } else if (functionName.toUpperCase().equals(GROUP_CONCAT)) {
                String groupConcatSeparator = DEFAULT_SEPERATOR;
                if (expr.expr().size() > 1) {
                    groupConcatSeparator = expr.getChild(functionColumnNr + 2).getText();
                }

                String transExpr = expr2AwkTranslater.translateSingleExprStmt(expr.expr(0));
                String tempName = functionName + "_" + i;
                if (isDistinct) {
                    String aggrTempName = "ag_" + tempName;
                    incStmt += String.format("%s[%s]=1;", tempName, transExpr);
                    resetStmt += String.format("delete %s; %s[%s]=1;", tempName, tempName, transExpr);
                    prepPrintStmt += String.format("%s=\"\"; for (n in %s) %s = %s n \"%s\";", aggrTempName, tempName,
                            aggrTempName, aggrTempName, groupConcatSeparator);
                    printStmt += "\"" + table.getDelimiter() + "\""
                            + String.format("substr(%s, 1, length(%s)-1)", aggrTempName, aggrTempName);
                } else {

                    beginStmtInit += tempName + "=\"\";";
                    incStmt += String.format("%s=%s %s \"%s\";", tempName, tempName, transExpr, groupConcatSeparator);
                    resetStmt += String.format("%s=%s \"%s\";", tempName, transExpr, groupConcatSeparator);
                    printStmt += "\"" + table.getDelimiter() + "\""
                            + String.format("substr(%s, 1, length(%s)-1)", tempName, tempName);
                }
            } else if (functionName.toUpperCase().equals(MAX)) {
                String transExpr = expr2AwkTranslater.translateSingleExprStmt(expr.expr(0));
                String tempName = functionName + "_" + i;
                beginStmtInit += tempName + "=-1e100;";
                incStmt += "if(" + transExpr + ">" + tempName + ")" + tempName + "=" + transExpr + ";";
                resetStmt += tempName + "=-1e100;";
                printStmt += "\"" + table.getDelimiter() + "\"" + tempName;

            } else if (functionName.toUpperCase().equals(MIN)) {
                String tempName = functionName + "_" + i;
                String transExpr = expr2AwkTranslater.translateSingleExprStmt(expr.expr(0));
                beginStmtInit += tempName + "=1e100;";
                incStmt += "if(" + transExpr + "<" + tempName + ")" + tempName + "=" + transExpr + ";";
                resetStmt += tempName + "=1e100;";
                printStmt += "\"" + table.getDelimiter() + "\"" + tempName;
            } else {
                continue;
                //throw new RuntimeException("Unknown function '" + functionName + "'");
            }

            table.addColumn(functionString, FieldType.INTEGER, false, table.getColumnCount());

            i++;
        }

        return new String[]{beginStmtInit, incStmt, resetStmt, prepPrintStmt, printStmt};
    }

    private String buildGroupingCountVarInitString(final BashSqlTable stmtTable,
                                                   final List<ExprContext> groupByExpressions, final String funcAdditions) {
        String result = "BEGIN {";

        int[] sortColumnNumbers = extractColumnNumbersFromGroupStmt(stmtTable, groupByExpressions);

        for (int sortColumnNumber : sortColumnNumbers) {
            result = result + "last_" + sortColumnNumber + "=0;";
        }

        return result.substring(0, result.length() - 1) + ";" + funcAdditions + "}";
    }

    private String buildGroupingContinueCountStmt(final BashSqlTable stmtTable,
                                                  final List<ExprContext> groupByExpressions, final String funcAdditions) {
        String result = "";

        int[] sortColumnNumbers = extractColumnNumbersFromGroupStmt(stmtTable, groupByExpressions);

        for (int sortColumnNumber : sortColumnNumbers) {
            result = result + "$" + sortColumnNumber + "==last_" + sortColumnNumber + "&&";
        }

        return result.substring(0, result.length() - 2) + " {lastrow=$0;" + funcAdditions + ";next}";
    }

    private String buildGroupingEndCountStmt(final BashSqlTable stmtTable, final List<ExprContext> groupByExpressions,
                                             final String printStmt, final String funcAdditions) {

        String resetVarStmt = "";

        int[] sortColumnNumbers = extractColumnNumbersFromGroupStmt(stmtTable, groupByExpressions);

        for (int sortColumnNumber : sortColumnNumbers) {

            resetVarStmt = resetVarStmt + "last_" + sortColumnNumber + "=$" + sortColumnNumber + ";";
        }

        return " {" + printStmt + ";"
                + resetVarStmt.substring(0, resetVarStmt.length() - 1) + ";" + funcAdditions + ";lastrow=$0}";
    }

    private String buildSortStatementFromMultipleGroupByColumns(final BashSqlTable stmtTable,
                                                                final List<ExprContext> groupByExpressions, final String delimiter) {

        int[] sortColumnNumbers = extractColumnNumbersFromGroupStmt(stmtTable, groupByExpressions);

        // build a sort statement
        ST template = new ST("<sort> -t $'<del>' <columns:{x | -k<x>,<x> }>");
        template.add("sort", programConfig.sort());
        template.add("del", delimiter);
        template.add("columns", sortColumnNumbers);
        return template.render();
    }

}
