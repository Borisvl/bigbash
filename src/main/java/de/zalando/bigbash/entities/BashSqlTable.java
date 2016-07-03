package de.zalando.bigbash.entities;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import de.zalando.bigbash.grammar.BashSqlParser;
import de.zalando.bigbash.pipes.BashInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * All column and table names are internally saved in lowercase letters.
 */
public class BashSqlTable {

    private static final Logger LOG = LoggerFactory.getLogger(BashSqlTable.class);

    public static class ColumnInformation {
        final int columnNr;
        final FieldType type;
        final boolean unique;

        public ColumnInformation(final int columnNr, final FieldType type, final boolean unique) {
            this.columnNr = columnNr;
            this.type = type;
            this.unique = unique;
        }

        public int getColumnNr() {
            return columnNr;
        }

        public FieldType getType() {
            return type;
        }

        public boolean isUnique() {
            return unique;
        }
    }

    private String tableName;
    private final Map<String, ColumnInformation> columns = Maps.newHashMap();
    private int columnCount = 0;
    private String delimiter;
    private BashInput input;

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    public String getTableName() {
        return tableName;
    }

    public BashInput getInput() {
        return input;
    }

    public void setInput(final BashInput input) {
        this.input = input;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public Map<String, ColumnInformation> getColumns() {
        return columns;
    }

    public void addColumn(final String table, final String name, final FieldType type, final boolean unique, final int columnNr) {
        addColumn(table.toLowerCase() + "." + name.toLowerCase(), type, unique, columnNr);
    }

    public void addColumn(final String table, final String name, final FieldType type, final int columnNr) {
        addColumn(table.toLowerCase() + "." + name.toLowerCase(), type, false, columnNr);
    }

    public void addColumn(final String name, final FieldType type, final boolean unique, final int columnNr) {
        ColumnInformation columnInfo = new ColumnInformation(columnNr, type, unique);
        columns.put(name.toLowerCase(), columnInfo);
        if (columnCount < columnNr + 1) {
            columnCount = columnNr + 1;
        }

        LOG.debug("Add new column {} of type {}", name, columnNr);
    }

    /**
     * @param  columnLiteral  Could be either the columnName or tablename.columnname
     */
    public ColumnInformation getColumnInformation(final String columnLiteral) {
        String lowerCaseColumnName = columnLiteral.toLowerCase();
        if (lowerCaseColumnName.indexOf('.') > 0) {
            return columns.get(lowerCaseColumnName);
        } else {
            ColumnInformation result = null;
            for (Map.Entry<String, ColumnInformation> entry : columns.entrySet()) {
                if (entry.getKey().equals(lowerCaseColumnName) || entry.getKey().endsWith("." + lowerCaseColumnName)) {
                    if (result != null) {
                        throw new RuntimeException("Query is ambiguous, column " + columnLiteral
                                + " may refer to different columns.");
                    }

                    result = entry.getValue();
                }

            }

            if (result == null) {
                LOG.debug("Could not find matching column to literal {}", columnLiteral);
            }

            return result;
        }
    }

    public String getColumnNameFromColumnNr(final int columnNr) {

        // Not optimal but does not matter
        for (Map.Entry<String, ColumnInformation> entry : columns.entrySet()) {
            if (entry.getValue().columnNr == columnNr) {
                return entry.getKey();
            }
        }

        throw new IndexOutOfBoundsException("No column with given column number " + columnNr + " in table "
                + tableName);
    }

    public Optional<ColumnInformation> getColumnInformation(final BashSqlParser.Column_name_defContext t) {

        String columnName = t.getText().toLowerCase();

        if (getColumnInformation(columnName) == null) {
            return Optional.absent();
        } else {
            return Optional.of(getColumnInformation(columnName));
        }
    }

    public ColumnInformation getColumnInformation(final int columnNr) {
        String name = getColumnNameFromColumnNr(columnNr);
        return getColumnInformation(name);
    }

    public int getColumnCount() {
        return columnCount;
    }

    public BashSqlTable createAlias(String name) {
        BashSqlTable copy = new BashSqlTable();
        copy.setTableName(name.toLowerCase());
        copy.setDelimiter(delimiter);
        copy.setInput(input);
        copy.columnCount = columnCount;
        for (Map.Entry<String, ColumnInformation> entry : columns.entrySet()) {
            String newKey = entry.getKey().replaceFirst("^[^\\.]*", name.toLowerCase());
            copy.columns.put(newKey, entry.getValue());
        }
        return copy;
    }
}
