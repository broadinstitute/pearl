package bio.terra.pearl.core.service.search.sql;

import bio.terra.pearl.core.service.search.BooleanOperator;
import lombok.Setter;
import org.jdbi.v3.core.statement.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SQLSearch {
    private static final String sqlFromTable = "enrollee";
    private static final String sqlFromAlias = "enrollee";
    private final List<SQLSelectClause> sqlSelectClauseList = new ArrayList<>();
    private final List<SQLJoinClause> sqlJoinClauseList = new ArrayList<>();

    /**
     * Recursive tree of SQLWhereClause objects
     */
    @Setter
    private SQLWhereClause sqlWhereClause;


    public String generateQueryString() {
        return String.format("SELECT enrollee.*%s FROM %s %s %s WHERE %s",
            generateSelectClause(),
            sqlFromTable,
            sqlFromAlias,
            generateJoinClause(),
            generateWhereClause());
    }

    public void bindSqlParams(Query query) {
        sqlWhereClause.bindSqlParams(query);
    }

    private String generateWhereClause() {
        SQLContext sqlContext = new SQLContext();
        return sqlWhereClause.generateSql(sqlContext);
    }

    private String generateSelectClause() {
        if (sqlSelectClauseList.isEmpty()) return "";

        return ", " + sqlSelectClauseList.stream()
            .map(SQLSelectClause::generateSql)
            .collect(Collectors.joining(", "));
    }

    private String generateJoinClause() {
        return sqlJoinClauseList.stream()
                .map(SQLJoinClause::generateSql)
                .collect(Collectors.joining(" "));
    }

    public void addSelectClause(SQLSelectClause selectClause) {
        if (sqlSelectClauseList
                .stream()
                .anyMatch(
                        existing -> existing.getAlias().equals(selectClause.getAlias())
                                && existing.getField().equals(selectClause.getField())))
            return;
        sqlSelectClauseList.add(selectClause);
    }

    public void addJoinClause(SQLJoinClause joinClause) {
        if (sqlJoinClauseList
                .stream()
                .anyMatch(
                        existing -> existing.getAlias().equals(joinClause.getAlias())
                                && existing.getTable().equals(joinClause.getTable())))
            return;
        sqlJoinClauseList.add(joinClause);
    }

    public SQLSearch merge(SQLSearch other, BooleanOperator operator) {

        for (SQLSelectClause selectClause : other.sqlSelectClauseList) {
            addSelectClause(selectClause);
        }

        for (SQLJoinClause joinClause : other.sqlJoinClauseList) {
            addJoinClause(joinClause);
        }

        sqlWhereClause = new SQLWhereBooleanExpression(sqlWhereClause, other.sqlWhereClause, operator);
        return this;
    }
}
