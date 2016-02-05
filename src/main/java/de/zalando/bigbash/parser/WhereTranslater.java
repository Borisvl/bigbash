package de.zalando.bigbash.parser;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.ProgramConfig;
import de.zalando.bigbash.grammar.BashSqlParser;
import org.aeonbits.owner.ConfigCache;

import java.util.List;

/**
 * Created by bvonloesch on 6/12/14.
 */
public class WhereTranslater {

    private final BashSqlTable table;
    private final String awk;

    public WhereTranslater(final BashSqlTable table) {
        this.table = table;
        awk = ConfigCache.getOrCreate(ProgramConfig.class).awk();
    }

    public String translateWhereExpression(final BashSqlParser.ExprContext expr) {
        ExprTranslater translater = new Expr2AwkTranslater2(this.table);
        String exprOutput = translater.translateSingleExprStmt(expr);
        return String.format("%s -F '%s' '(%s) {print}'", awk, table.getDelimiter(), exprOutput);
    }

    /**
     * Many expressions that must hold simultanously (aka AND).
     */
    public String translateWhereExpression(final List<BashSqlParser.ExprContext> exprs) {
        final ExprTranslater translater = new Expr2AwkTranslater2(this.table);
        String query = Joiner.on(" && ").join(Iterables.transform(exprs,
                    new Function<BashSqlParser.ExprContext, String>() {
                        @Override
                        public String apply(final BashSqlParser.ExprContext exprContext) {
                            return translater.translateSingleExprStmt(exprContext);
                        }
                    }));
        return String.format("%s -F '%s' '(%s) {print}'", awk, table.getDelimiter(), query);
    }
}
