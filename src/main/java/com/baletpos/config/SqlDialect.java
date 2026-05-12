package com.baletpos.config;

public final class SqlDialect {
    private SqlDialect() {
    }

    public static String nowExpression() {
        if (DatabaseConfig.getDialect() == DatabaseDialect.POSTGRES) {
            return "now()";
        }
        return "datetime('now', 'localtime')";
    }

    public static String dateExpression(String column) {
        if (DatabaseConfig.getDialect() == DatabaseDialect.POSTGRES) {
            return "CAST(" + column + " AS date)";
        }
        return "date(" + column + ")";
    }

    public static String lastInsertIdExpression() {
        if (DatabaseConfig.getDialect() == DatabaseDialect.POSTGRES) {
            return "lastval()";
        }
        return "last_insert_rowid()";
    }
}
