package de.zalando.bigbash.parser;

import de.zalando.bigbash.grammar.BashSqlParser;

/**
 * Created by bvonloesch on 6/28/14.
 */
public interface ExprTranslater {

    String translateSingleExprStmt(final BashSqlParser.ExprContext expr);
}
