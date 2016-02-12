package de.zalando.bigbash.parser;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.CompressionType;
import de.zalando.bigbash.entities.FileMappingProperties;
import de.zalando.bigbash.grammar.BashSqlBaseListener;
import de.zalando.bigbash.grammar.BashSqlParser;
import de.zalando.bigbash.pipes.BashMissingInput;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.Map;

public class BashSqlGeneralStmtListener extends BashSqlBaseListener {

    protected final Map<String, BashSqlTable> createdTablespace = Maps.newHashMap();
    private final boolean useSortAggregation;
    private Map<String, FileMappingProperties> fileMappingPropertiesMap = Maps.newHashMap();
    private String output;

    public BashSqlGeneralStmtListener(final Map<String, FileMappingProperties> fileMappingPropertiesMap,
                                      boolean useSortAggregation) {
        this.fileMappingPropertiesMap = fileMappingPropertiesMap;
        this.useSortAggregation = useSortAggregation;
    }

    private static String getArgument(@NotNull String text) {
        String arg = text;
        if (arg.startsWith("'")) {
            arg = arg.substring(1, arg.length() - 1);
        }
        return arg;
    }

    @Override
    public void enterCreate_table_stmt(@NotNull final BashSqlParser.Create_table_stmtContext ctx) {

        // fork to BashSqlCreateStmtListener
        ParseTreeWalker walker = new ParseTreeWalker();

        // Walk the tree created during the parse, trigger callbacks
        BashSqlCreateStmtListener listener = new BashSqlCreateStmtListener();
        walker.walk(listener, ctx);

        BashSqlTable createdTable = listener.createdTable;

        // Find file properties
        FileMappingProperties properties = fileMappingPropertiesMap.get(createdTable.getTableName().toLowerCase());
        if (properties == null) {
            //Throws an error if a select statement tries to read from it
            createdTable.setDelimiter(";");
            createdTable.setInput(new BashMissingInput("Could not find file mapping for table " + createdTable.getTableName()));
        } else {
            createdTable.setDelimiter(properties.getDelimiter());
            createdTable.setInput(properties.getPipeInput());
        }
        // store the created table
        createdTablespace.put(createdTable.getTableName(), createdTable);
    }

    @Override
    public void enterMap_stmt(@NotNull BashSqlParser.Map_stmtContext ctx) {
        //TODO:Refactor FileMappingProperties
        CompressionType type = CompressionType.NONE;
        if (ctx.type != null) {
            String ctxType = getArgument(ctx.type.getText().toUpperCase());
            if ("GZ".equals(ctxType)) {
                type = CompressionType.GZ;
            } else if ("RAW".equals(ctxType)) {
                type = CompressionType.RAW;
            } else if ("SQLITE3".equals(ctxType)) {
                type = CompressionType.SQLITE3;
            } else if ("FILE".equals(ctxType)) {
                type = CompressionType.NONE;
            } else {
                throw new RuntimeException("Unknown type " + ctx.type.getText());
            }
        }
        String delimiter = "\\t";
        if (ctx.delimiter != null) {
            delimiter = getArgument(ctx.delimiter.getText());
            if (delimiter.length() == 0) {
                throw new RuntimeException("Delimiter must be at least one character");
            }
        }
        String tableName = ctx.table_name().getText().toLowerCase();
        boolean removeHeader = false;
        if (ctx.K_REMOVEHEADER() != null) {
            removeHeader = true;
        }
        Optional<Character> quoteChar = Optional.absent();
        if (ctx.quote != null) {
            String quote = getArgument(ctx.quote.getText());
            if (quote.length() > 1) {
                throw new RuntimeException("Quote character must be a single character");
            }
            quoteChar = Optional.of(quote.charAt(0));
        }
        FileMappingProperties properties = new FileMappingProperties(getArgument(ctx.to.getText()), type,
                delimiter, removeHeader, quoteChar);
        fileMappingPropertiesMap.put(tableName,
                properties);
        if (createdTablespace.containsKey(tableName)) {
            BashSqlTable table = createdTablespace.get(tableName);
            table.setDelimiter(properties.getDelimiter());
            table.setInput(properties.getPipeInput());
        }
    }

    @Override
    public void enterSelect_stmt(@NotNull final BashSqlParser.Select_stmtContext ctx) {
        BashSqlSelectListener selectListener = new BashSqlSelectListener();
        selectListener.enterSelect_stmt(ctx);

        BashSqlSelectTranslater selectTranslater = new BashSqlSelectTranslater(createdTablespace, useSortAggregation);
        output = selectTranslater.getSelectExpression(selectListener.getSelectStmtData());
    }

    public Map<String, BashSqlTable> getCreatedTablespace() {
        return createdTablespace;
    }

    public String getOutput() {
        return output;
    }
}