package de.zalando.bigbash.parser;

import de.zalando.bigbash.grammar.BashSqlParser;

/**
 * Created by bvonloesch on 05/06/16.
 */
public class CaseWhen2AwkTranslater {

    public String translateSingleExprStmt(BashSqlParser.Case_expressionContext expr, ExprTranslater translater) {
        StringBuilder output = new StringBuilder();
        output.append('(');
        if (expr.arg1 != null) {
            //Switch statement
            String switchExpression = translater.translateSingleExprStmt(expr.arg1);

            int nrOfWhereThenParts = expr.expr().size() - 1;
            if (expr.K_ELSE() != null) nrOfWhereThenParts--;
            if (nrOfWhereThenParts % 2 != 0) {
                throw new RuntimeException("Something wrong with CASE statement");
            }
            nrOfWhereThenParts /= 2;

            for (int i = 0; i < nrOfWhereThenParts; i++) {
                if (i > 0) output.append(" : ");
                output.append('(').append(switchExpression).append(") == ")
                        .append(translater.translateSingleExprStmt(expr.expr(2 * i + 1)))
                        .append("? ").append(translater.translateSingleExprStmt(expr.expr(2 * i + 2)));
            }

        } else {
            //If statement
            int nrOfWhereThenParts = expr.expr().size();
            if (expr.K_ELSE() != null) nrOfWhereThenParts--;
            if (nrOfWhereThenParts % 2 != 0) {
                throw new RuntimeException("Something wrong with CASE statement");
            }
            nrOfWhereThenParts /= 2;

            for (int i = 0; i < nrOfWhereThenParts; i++) {
                if (i > 0) output.append(" : ");
                output.append(translater.translateSingleExprStmt(expr.expr(2 * i)))
                        .append("? ").append(translater.translateSingleExprStmt(expr.expr(2 * i + 1)));
            }
        }
        if (expr.K_ELSE() != null) {
            output.append(" : ").append(translater.translateSingleExprStmt(expr.expr(expr.expr().size() - 1)));
        } else {
            output.append(" : \"\"");
        }
        output.append(')');
        return output.toString();
    }
}
