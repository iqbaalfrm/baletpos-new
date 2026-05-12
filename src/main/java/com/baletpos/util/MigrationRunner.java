package com.baletpos.util;

import com.baletpos.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MigrationRunner {
    private static final Logger logger = LoggerFactory.getLogger(MigrationRunner.class);

    public static void runMigrations() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Check essential columns in sales
            checkAndAddColumn(conn, "sales", "technician_id", "INTEGER");
            checkAndAddColumn(conn, "sales", "void_reason", "TEXT");
            checkAndAddColumn(conn, "sales", "voided_by", "INTEGER");
            checkAndAddColumn(conn, "sales", "voided_at", "TEXT");
            checkAndAddColumn(conn, "sales", "total_hpp", "INTEGER DEFAULT 0");

            // Check sale_items
            checkAndAddColumn(conn, "sale_items", "hpp_per_unit", "INTEGER DEFAULT 0");

            // Ensure new tables for Returns and Mutations
            ensureTables(conn);

            logger.info("Migration check completed successfully");

        } catch (Exception e) {
            logger.error("Migration check failed", e);
        }
    }

    private static void ensureTables(Connection conn) {
        String[] sqls = {
                // Sales Returns
                "CREATE TABLE IF NOT EXISTS sales_returns (id INTEGER PRIMARY KEY AUTOINCREMENT, return_no TEXT UNIQUE, sale_id INTEGER, user_id INTEGER, created_at TEXT, notes TEXT, total_amount INTEGER DEFAULT 0)",

                "CREATE TABLE IF NOT EXISTS sales_return_items (id INTEGER PRIMARY KEY AUTOINCREMENT, sales_return_id INTEGER, product_id INTEGER, qty_return INTEGER, unit_price INTEGER, snapshot_hpp INTEGER, line_total INTEGER, created_at TEXT)",

                // Purchase Returns
                "CREATE TABLE IF NOT EXISTS purchase_returns (id INTEGER PRIMARY KEY AUTOINCREMENT, pr_no TEXT UNIQUE, purchase_id INTEGER, user_id INTEGER, created_at TEXT, notes TEXT, total_amount INTEGER DEFAULT 0)",

                "CREATE TABLE IF NOT EXISTS purchase_return_items (id INTEGER PRIMARY KEY AUTOINCREMENT, purchase_return_id INTEGER, product_id INTEGER, qty_return INTEGER, unit_price INTEGER, line_total INTEGER, created_at TEXT)",

                // Stock Adjustments
                "CREATE TABLE IF NOT EXISTS stock_adjustments (id INTEGER PRIMARY KEY AUTOINCREMENT, adj_no TEXT UNIQUE, user_id INTEGER, reason TEXT, created_at TEXT)",

                "CREATE TABLE IF NOT EXISTS stock_adjustment_items (id INTEGER PRIMARY KEY AUTOINCREMENT, stock_adjustment_id INTEGER, product_id INTEGER, qty_delta INTEGER, note TEXT, created_at TEXT)",

                // Opening Stocks
                "CREATE TABLE IF NOT EXISTS opening_stocks (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER, period_yyyymm TEXT, opening_qty INTEGER DEFAULT 0, created_by INTEGER, created_at TEXT, UNIQUE(product_id, period_yyyymm))",

                // Invoice Sequences
                "CREATE TABLE IF NOT EXISTS invoice_sequences (id INTEGER PRIMARY KEY AUTOINCREMENT, prefix TEXT NOT NULL, date_part TEXT NOT NULL, last_number INTEGER NOT NULL DEFAULT 0, UNIQUE(prefix, date_part))",

                // Audit Logs
                "CREATE TABLE IF NOT EXISTS audit_logs (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, action TEXT NOT NULL, table_name TEXT NOT NULL, record_id INTEGER, old_values TEXT, new_values TEXT, ip_address TEXT, created_at TEXT DEFAULT (datetime('now', 'localtime')))"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : sqls) {
                stmt.execute(sql);
            }
            logger.info("Schema tables verified/created");
        } catch (Exception e) {
            logger.error("Failed to create tables", e);
        }
    }

    private static void checkAndAddColumn(Connection conn, String table, String column, String type) {
        try {
            boolean exists = false;
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
                while (rs.next()) {
                    if (column.equalsIgnoreCase(rs.getString("name"))) {
                        exists = true;
                        break;
                    }
                }
            }

            if (!exists) {
                logger.info("Adding missing column {} to table {}", column, table);
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to add column {} to {}: {}", column, table, e.getMessage());
        }
    }
}


