package de.zalando.bigbash.commands;

import de.zalando.bigbash.entities.BashSqlTable;
import de.zalando.bigbash.entities.JoinType;

public interface TableJoiner {
    BashSqlTable join(BashSqlTable table1, BashSqlTable table2, String columnName1, String columnName2,
            final JoinType joinType);
}
