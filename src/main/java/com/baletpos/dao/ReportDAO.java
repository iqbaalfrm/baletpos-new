package com.baletpos.dao;

import com.baletpos.config.DatabaseConfig;
import com.baletpos.config.DatabaseDialect;
import com.baletpos.config.SqlDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object untuk berbagai Report
 */
public class ReportDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReportDAO.class);

    /**
     * Report Data class untuk menampung hasil query report
     */
    public static class ReportRow {
        private Map<String, Object> data = new HashMap<>();

        public void put(String key, Object value) {
            data.put(key, value);
        }

        public Object get(String key) {
            return data.get(key);
        }

        public String getString(String key) {
            Object val = data.get(key);
            return val != null ? val.toString() : "";
        }

        public Long getLong(String key) {
            Object val = data.get(key);
            if (val instanceof Number) {
                return ((Number) val).longValue();
            }
            return 0L;
        }

        public Integer getInt(String key) {
            Object val = data.get(key);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
            return 0;
        }

        public BigDecimal getBigDecimal(String key) {
            Object val = data.get(key);
            if (val instanceof Number) {
                return BigDecimal.valueOf(((Number) val).longValue());
            }
            return BigDecimal.ZERO;
        }

        public Map<String, Object> getData() {
            return data;
        }
    }

    // ========================================
    // REPORT PENJUALAN LAPTOP
    // ========================================
    public List<ReportRow> getSalesReportLaptop(LocalDate startDate, LocalDate endDate) {
        // Get combined laptop sales (both NEW and SECOND)
        List<ReportRow> newLaptops = getSalesReportByProductType(startDate, endDate, "LAPTOP_NEW");
        List<ReportRow> secondLaptops = getSalesReportByProductType(startDate, endDate, "LAPTOP_SECOND");
        List<ReportRow> combined = new ArrayList<>(newLaptops);
        combined.addAll(secondLaptops);
        return combined;
    }

    // ========================================
    // REPORT PENJUALAN PERIPHERAL
    // ========================================
    public List<ReportRow> getSalesReportPeripheral(LocalDate startDate, LocalDate endDate) {
        return getSalesReportByProductType(startDate, endDate, "PERIPHERAL");
    }

    // ========================================
    // REPORT PENJUALAN SERVICE
    // ========================================
    public List<ReportRow> getSalesReportService(LocalDate startDate, LocalDate endDate) {
        return getSalesReportByProductType(startDate, endDate, "SERVICE");
    }

    public List<ReportRow> getSalesReportByProductType(LocalDate startDate, LocalDate endDate, String productType) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<ReportRow> rows = new ArrayList<>();
        String sql = "SELECT " +
                "p.sku, " +
                "p.name as product_name, " +
                "SUM(si.quantity) as qty_sold, " +
                "SUM(si.subtotal) as revenue, " +
                "SUM(si.quantity * si.hpp_per_unit) as cogs " +
                "FROM sale_items si " +
                "INNER JOIN sales s ON si.sale_id = s.id " +
                "INNER JOIN products p ON si.product_id = p.id " +
                "WHERE s.status = 'COMPLETED' " +
                "AND p.product_type_code = ? " +
                "AND s.sale_date BETWEEN ? AND ? " +
                "GROUP BY p.id, p.sku, p.name " +
                "ORDER BY revenue DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productType);
            setDateTimeRange(pstmt, 2, 3, start, end);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.put("sku", rs.getString("sku"));
                    row.put("product_name", rs.getString("product_name"));
                    row.put("qty_sold", rs.getInt("qty_sold"));
                    row.put("revenue", rs.getLong("revenue"));
                    row.put("cogs", rs.getLong("cogs"));
                    row.put("gross_profit", rs.getLong("revenue") - rs.getLong("cogs"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting sales report for {}", productType, e);
        }
        return rows;
    }

    // ========================================
    // REPORT ASET / INVENTORY VALUATION
    // ========================================
    public List<ReportRow> getInventoryValuationReport() {
        List<ReportRow> rows = new ArrayList<>();
        String sql = "SELECT " +
                "p.sku, " +
                "p.name as product_name, " +
                "p.product_type_code, " +
                "p.stock as stock_qty, " +
                "p.hpp, " +
                "p.selling_price, " +
                "(p.stock * p.hpp) as total_hpp_value, " +
                "(p.stock * p.selling_price) as total_sell_value " +
                "FROM products p " +
                "WHERE p.is_active = 1 " +
                "ORDER BY p.product_type_code, p.sku";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ReportRow row = new ReportRow();
                row.put("sku", rs.getString("sku"));
                row.put("product_name", rs.getString("product_name"));
                row.put("product_type", rs.getString("product_type_code"));
                row.put("stock_qty", rs.getInt("stock_qty"));
                row.put("hpp", rs.getLong("hpp"));
                row.put("selling_price", rs.getLong("selling_price"));
                row.put("total_hpp_value", rs.getLong("total_hpp_value"));
                row.put("total_sell_value", rs.getLong("total_sell_value"));
                rows.add(row);
            }
        } catch (SQLException e) {
            logger.error("Error getting inventory valuation report", e);
        }
        return rows;
    }

    public ReportRow getInventoryTotals() {
        ReportRow totals = new ReportRow();
        String sql = "SELECT " +
                "SUM(stock) as total_qty, " +
                "SUM(stock * hpp) as total_hpp_value, " +
                "SUM(stock * selling_price) as total_sell_value " +
                "FROM products WHERE is_active = 1";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                totals.put("total_qty", rs.getInt("total_qty"));
                totals.put("total_hpp_value", rs.getLong("total_hpp_value"));
                totals.put("total_sell_value", rs.getLong("total_sell_value"));
            }
        } catch (SQLException e) {
            logger.error("Error getting inventory totals", e);
        }
        return totals;
    }

    // ========================================
    // REPORT LABA RUGI (PROFIT & LOSS)
    // ========================================
    public ReportRow getProfitLossReport(LocalDate startDate, LocalDate endDate) {
        ReportRow report = new ReportRow();

        try (Connection conn = DatabaseConfig.getConnection()) {
            // 1. REVENUE: Total penjualan COMPLETED
            BigDecimal grossRevenue = getGrossSalesRevenue(conn, startDate, endDate);

            // 2. SALES RETURNS: Total retur penjualan
            BigDecimal salesReturns = getSalesReturnValue(conn, startDate, endDate);

            // 3. NET REVENUE
            BigDecimal netRevenue = grossRevenue.subtract(salesReturns);

            // 4. COGS: HPP dari penjualan COMPLETED
            BigDecimal grossCogs = getGrossCogs(conn, startDate, endDate);

            // 5. COGS REVERSAL: HPP dari retur penjualan
            BigDecimal cogsReversal = getSalesReturnCogs(conn, startDate, endDate);

            // 6. NET COGS
            BigDecimal netCogs = grossCogs.subtract(cogsReversal);

            // 7. GROSS PROFIT
            BigDecimal grossProfit = netRevenue.subtract(netCogs);

            // 8. EXPENSES
            BigDecimal totalExpense = getTotalExpenses(conn, startDate, endDate);

            // 9. NET PROFIT
            BigDecimal netProfit = grossProfit.subtract(totalExpense);

            // Fill report
            report.put("gross_revenue", grossRevenue);
            report.put("sales_returns", salesReturns);
            report.put("net_revenue", netRevenue);
            report.put("gross_cogs", grossCogs);
            report.put("cogs_reversal", cogsReversal);
            report.put("net_cogs", netCogs);
            report.put("gross_profit", grossProfit);
            report.put("total_expense", totalExpense);
            report.put("net_profit", netProfit);
            report.put("start_date", startDate.toString());
            report.put("end_date", endDate.toString());

            // Gross margin percentage
            if (netRevenue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal grossMargin = grossProfit.multiply(BigDecimal.valueOf(100))
                        .divide(netRevenue, 2, java.math.RoundingMode.HALF_UP);
                report.put("gross_margin_percent", grossMargin);

                BigDecimal netMargin = netProfit.multiply(BigDecimal.valueOf(100))
                        .divide(netRevenue, 2, java.math.RoundingMode.HALF_UP);
                report.put("net_margin_percent", netMargin);
            } else {
                report.put("gross_margin_percent", BigDecimal.ZERO);
                report.put("net_margin_percent", BigDecimal.ZERO);
            }

        } catch (SQLException e) {
            logger.error("Error generating profit/loss report", e);
        }

        return report;
    }

    private BigDecimal getGrossSalesRevenue(Connection conn, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total " +
                "FROM sales WHERE status = 'COMPLETED' AND sale_date BETWEEN ? AND ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setDateTimeRange(pstmt, 1, 2, start, end);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return BigDecimal.valueOf(rs.getLong("total"));
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getSalesReturnValue(Connection conn, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total " +
                "FROM sales_returns WHERE status = 'COMPLETED' AND return_date BETWEEN ? AND ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setDateTimeRange(pstmt, 1, 2, start, end);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return BigDecimal.valueOf(rs.getLong("total"));
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getGrossCogs(Connection conn, LocalDate startDate, LocalDate endDate) throws SQLException {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        String sql = "SELECT COALESCE(SUM(si.quantity * si.hpp_per_unit), 0) as total " +
                "FROM sale_items si " +
                "INNER JOIN sales s ON si.sale_id = s.id " +
                "WHERE s.status = 'COMPLETED' AND s.sale_date BETWEEN ? AND ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setDateTimeRange(pstmt, 1, 2, start, end);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return BigDecimal.valueOf(rs.getLong("total"));
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getSalesReturnCogs(Connection conn, LocalDate startDate, LocalDate endDate) throws SQLException {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        String sql = "SELECT COALESCE(SUM(sri.quantity * sri.hpp_per_unit), 0) as total " +
                "FROM sales_return_items sri " +
                "INNER JOIN sales_returns sr ON sri.sales_return_id = sr.id " +
                "WHERE sr.status = 'COMPLETED' AND sr.return_date BETWEEN ? AND ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setDateTimeRange(pstmt, 1, 2, start, end);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return BigDecimal.valueOf(rs.getLong("total"));
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getTotalExpenses(Connection conn, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total " +
                "FROM expenses WHERE " + SqlDialect.dateExpression("expense_date") + " BETWEEN ? AND ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return BigDecimal.valueOf(rs.getLong("total"));
                }
            }
        }
        return BigDecimal.ZERO;
    }

    // ========================================
    // REPORT BIAYA OPERASIONAL
    // ========================================
    public List<ReportRow> getExpenseReport(LocalDate startDate, LocalDate endDate) {
        List<ReportRow> rows = new ArrayList<>();
        String sql = "SELECT " +
                "ec.code as expense_code, " +
                "ec.name as expense_name, " +
                "SUM(e.amount) as total_amount, " +
                "COUNT(e.id) as transaction_count " +
                "FROM expenses e " +
                "INNER JOIN expense_codes ec ON e.expense_code_id = ec.id " +
                "WHERE " + SqlDialect.dateExpression("e.expense_date") + " BETWEEN ? AND ? " +
                "GROUP BY ec.id, ec.code, ec.name " +
                "ORDER BY total_amount DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.put("expense_code", rs.getString("expense_code"));
                    row.put("expense_name", rs.getString("expense_name"));
                    row.put("total_amount", rs.getLong("total_amount"));
                    row.put("transaction_count", rs.getInt("transaction_count"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting expense report", e);
        }
        return rows;
    }

    // ========================================
    // DASHBOARD SUMMARY
    // ========================================
    public ReportRow getDashboardSummary(LocalDate date) {
        ReportRow summary = new ReportRow();

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Today's sales
            String sqlTodaySales = "SELECT COUNT(*) as count, COALESCE(SUM(total_amount), 0) as total " +
                    "FROM sales WHERE status = 'COMPLETED' AND sale_date BETWEEN ? AND ?";

            LocalDateTime todayStart = date.atStartOfDay();
            LocalDateTime todayEnd = date.atTime(23, 59, 59);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlTodaySales)) {
                setDateTimeRange(pstmt, 1, 2, todayStart, todayEnd);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        summary.put("today_sales_count", rs.getInt("count"));
                        summary.put("today_sales_total", rs.getLong("total"));
                    }
                }
            }

            // Low stock products (Now just Out of Stock)
            String sqlLowStock = "SELECT COUNT(*) as count FROM products WHERE is_active = 1 AND stock <= 0";
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sqlLowStock)) {
                if (rs.next()) {
                    summary.put("low_stock_count", rs.getInt("count"));
                }
            }

            // Total products
            String sqlTotalProducts = "SELECT COUNT(*) as count FROM products WHERE is_active = 1";
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sqlTotalProducts)) {
                if (rs.next()) {
                    summary.put("total_products", rs.getInt("count"));
                }
            }

            // This month revenue
            LocalDate monthStart = date.withDayOfMonth(1);
            LocalDate monthEnd = date.withDayOfMonth(date.lengthOfMonth());

            String sqlMonthRevenue = "SELECT COALESCE(SUM(total_amount), 0) as total " +
                    "FROM sales WHERE status = 'COMPLETED' AND sale_date BETWEEN ? AND ?";

            LocalDateTime monthStartDateTime = monthStart.atStartOfDay();
            LocalDateTime monthEndDateTime = monthEnd.atTime(23, 59, 59);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlMonthRevenue)) {
                setDateTimeRange(pstmt, 1, 2, monthStartDateTime, monthEndDateTime);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        summary.put("month_revenue", rs.getLong("total"));
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Error getting dashboard summary", e);
        }

        return summary;
    }

    // ========================================
    // STOCK MOVEMENT REPORT
    // ========================================
    public List<ReportRow> getStockMovementReport(Long productId, LocalDate startDate, LocalDate endDate) {
        List<ReportRow> rows = new ArrayList<>();
        String sql = "SELECT " +
                "sm.id, " +
                "sm.movement_type, " +
                "sm.reference_type, " +
                "sm.reference_id, " +
                "sm.quantity_change, " +
                "sm.stock_before, " +
                "sm.stock_after, " +
                "sm.notes, " +
                "sm.created_at, " +
                "u.full_name as created_by_name " +
                "FROM stock_movements sm " +
                "LEFT JOIN users u ON sm.created_by = u.id " +
                "WHERE sm.product_id = ? AND " + SqlDialect.dateExpression("sm.created_at") + " BETWEEN ? AND ? " +
                "ORDER BY sm.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, productId);
            pstmt.setString(2, startDate.toString());
            pstmt.setString(3, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.put("id", rs.getLong("id"));
                    row.put("movement_type", rs.getString("movement_type"));
                    row.put("reference_type", rs.getString("reference_type"));
                    row.put("reference_id", rs.getLong("reference_id"));
                    row.put("quantity_change", rs.getInt("quantity_change"));
                    row.put("stock_before", rs.getInt("stock_before"));
                    row.put("stock_after", rs.getInt("stock_after"));
                    row.put("notes", rs.getString("notes"));
                    row.put("created_at", rs.getString("created_at"));
                    row.put("created_by_name", rs.getString("created_by_name"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting stock movement report", e);
        }
        return rows;
    }

    private void setDateTimeRange(PreparedStatement pstmt, int startIndex, int endIndex,
            LocalDateTime start, LocalDateTime end) throws SQLException {
        if (DatabaseConfig.getDialect() == DatabaseDialect.POSTGRES) {
            pstmt.setTimestamp(startIndex, Timestamp.valueOf(start));
            pstmt.setTimestamp(endIndex, Timestamp.valueOf(end));
            return;
        }

        pstmt.setString(startIndex, start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        pstmt.setString(endIndex, end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}


