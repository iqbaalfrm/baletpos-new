package com.baletpos.controller;

import com.baletpos.config.DatabaseConfig;
import com.baletpos.config.DatabaseDialect;
import com.baletpos.dao.ReportDAO;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controller untuk Dashboard Content - Style BaletPOS
 */
public class DashboardContentController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardContentController.class);

    private DashboardController parentController;

    // KPI Labels
    @FXML
    private Label todaySalesLabel;
    @FXML
    private Label salesTrendLabel;
    @FXML
    private Label todayTransactionLabel;
    @FXML
    private Label transactionTrendLabel;
    @FXML
    private Label monthExpenseLabel;
    @FXML
    private Label expenseTrendLabel;
    @FXML
    private Label dbStatusLabel;

    // Chart
    @FXML
    private LineChart<String, Number> salesTrendChart;
    @FXML
    private CategoryAxis chartXAxis;
    @FXML
    private NumberAxis chartYAxis;
    @FXML
    private Label chartSummaryLabel;
    @FXML
    private Label growthBadge;
    @FXML
    private VBox latestTransactionsContainer;

    // Top Products
    @FXML
    private VBox topProductsContainer;
    @FXML
    private Label topProduct1Name;
    @FXML
    private Label topProduct1Sku;
    @FXML
    private Label topProduct1Qty;
    @FXML
    private Label topProduct2Name;
    @FXML
    private Label topProduct2Sku;
    @FXML
    private Label topProduct2Qty;
    @FXML
    private Label topProduct3Name;
    @FXML
    private Label topProduct3Sku;
    @FXML
    private Label topProduct3Qty;

    // Low Stock
    @FXML
    private VBox lowStockContainer;
    @FXML
    private Label lowStock1Name;
    @FXML
    private Label lowStockTotalLabel;
    @FXML
    private Label lowStock1Qty;
    @FXML
    private Label lowStock2Name;
    @FXML
    private Label lowStock2Qty;
    @FXML
    private Label lowStock3Name;
    @FXML
    private Label lowStock3Qty;
    @FXML
    private Button restock1Btn;
    @FXML
    private Button restock2Btn;
    @FXML
    private Button restock3Btn;

    private final ReportDAO reportDAO = new ReportDAO();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

    public DashboardContentController() {
        currencyFormat.setMaximumFractionDigits(0);
    }

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        try {
            loadDatabaseStatus();
            loadKpiData();
            loadSalesTrendChart();
            loadTopProducts();
            loadLatestTransactions();
            loadLowStockProducts();
        } catch (Exception e) {
            logger.error("Error loading dashboard data", e);
        }
    }

    private void loadDatabaseStatus() {
        if (dbStatusLabel == null) {
            return;
        }

        if (DatabaseConfig.getDialect() == DatabaseDialect.POSTGRES) {
            dbStatusLabel.setText("DB: Supabase / PostgreSQL");
            dbStatusLabel.setStyle(
                    "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 4; -fx-padding: 6 10; -fx-font-size: 11px; -fx-font-weight: 900;");
        } else {
            dbStatusLabel.setText("DB: SQLite lokal");
            dbStatusLabel.setStyle(
                    "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 4; -fx-padding: 6 10; -fx-font-size: 11px; -fx-font-weight: 900;");
        }
    }

    private void loadKpiData() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Today's sales
        ReportDAO.ReportRow todaySummary = reportDAO.getDashboardSummary(today);
        ReportDAO.ReportRow yesterdaySummary = reportDAO.getDashboardSummary(yesterday);

        BigDecimal todaySales = todaySummary.getBigDecimal("today_sales_total");
        int todayCount = todaySummary.getInt("today_sales_count");
        BigDecimal yesterdaySales = yesterdaySummary.getBigDecimal("today_sales_total");
        int yesterdayCount = yesterdaySummary.getInt("today_sales_count");

        if (todaySales == null)
            todaySales = BigDecimal.ZERO;
        if (yesterdaySales == null)
            yesterdaySales = BigDecimal.ZERO;

        // Set today's sales label
        if (todaySalesLabel != null) {
            todaySalesLabel.setText(currencyFormat.format(todaySales));
        }

        // Set transaction count
        if (todayTransactionLabel != null) {
            todayTransactionLabel.setText(String.valueOf(todayCount));
        }

        // Calculate sales trend
        if (salesTrendLabel != null) {
            String trend = calculateTrend(todaySales, yesterdaySales);
            salesTrendLabel.setText(trend + " vs Kemarin");
            if (trend.startsWith("+")) {
                salesTrendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1E40AF;");
            } else if (trend.startsWith("-")) {
                salesTrendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #DC2626;");
            }
        }

        // Calculate transaction trend
        if (transactionTrendLabel != null) {
            String trend = calculateTrendInt(todayCount, yesterdayCount);
            transactionTrendLabel.setText(trend + " vs Kemarin");
            if (trend.startsWith("+")) {
                transactionTrendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1E40AF;");
            } else if (trend.startsWith("-")) {
                transactionTrendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #DC2626;");
            }
        }

        // Month expense
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate prevMonthStart = monthStart.minusMonths(1);
        LocalDate prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth());

        ReportDAO.ReportRow thisMonthReport = reportDAO.getProfitLossReport(monthStart, monthEnd);
        ReportDAO.ReportRow prevMonthReport = reportDAO.getProfitLossReport(prevMonthStart, prevMonthEnd);

        BigDecimal thisMonthExpense = thisMonthReport.getBigDecimal("total_expense");
        BigDecimal prevMonthExpense = prevMonthReport.getBigDecimal("total_expense");

        if (thisMonthExpense == null)
            thisMonthExpense = BigDecimal.ZERO;
        if (prevMonthExpense == null)
            prevMonthExpense = BigDecimal.ZERO;

        if (monthExpenseLabel != null) {
            monthExpenseLabel.setText(currencyFormat.format(thisMonthExpense));
        }

        if (expenseTrendLabel != null) {
            String trend = calculateTrend(thisMonthExpense, prevMonthExpense);
            expenseTrendLabel.setText(trend + " vs Bulan Lalu");
            // For expense, negative is good
            if (trend.startsWith("-")) {
                expenseTrendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1E40AF;");
            } else if (trend.startsWith("+")) {
                expenseTrendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #DC2626;");
            }
        }
    }

    private String calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current != null && current.compareTo(BigDecimal.ZERO) > 0) {
                return "+100%";
            }
            return "0%";
        }

        BigDecimal diff = current.subtract(previous);
        BigDecimal pct = diff.multiply(BigDecimal.valueOf(100)).divide(previous, 0, RoundingMode.HALF_UP);

        if (pct.compareTo(BigDecimal.ZERO) >= 0) {
            return "+" + pct.intValue() + "%";
        } else {
            return pct.intValue() + "%";
        }
    }

    private String calculateTrendInt(int current, int previous) {
        if (previous == 0) {
            if (current > 0) {
                return "+100%";
            }
            return "0%";
        }

        int diff = current - previous;
        int pct = (diff * 100) / previous;

        if (pct >= 0) {
            return "+" + pct + "%";
        } else {
            return pct + "%";
        }
    }

    private void loadSalesTrendChart() {
        if (salesTrendChart == null)
            return;

        salesTrendChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Penjualan");

        LocalDate today = LocalDate.now();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE", Locale.of("id", "ID"));

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal firstDaySales = BigDecimal.ZERO;
        BigDecimal lastDaySales = BigDecimal.ZERO;

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            ReportDAO.ReportRow dayReport = reportDAO.getDashboardSummary(day);
            BigDecimal revenue = dayReport.getBigDecimal("today_sales_total");
            if (revenue == null)
                revenue = BigDecimal.ZERO;

            totalSales = totalSales.add(revenue);

            if (i == 6)
                firstDaySales = revenue;
            if (i == 0)
                lastDaySales = revenue;

            String dayLabel = day.format(dayFmt);
            series.getData().add(new XYChart.Data<>(dayLabel, revenue.doubleValue() / 1_000_000));
        }

        salesTrendChart.getData().add(series);

        // Update summary
        if (chartSummaryLabel != null) {
            chartSummaryLabel.setText("Total 7 hari: " + currencyFormat.format(totalSales));
        }

        // Update growth badge
        if (growthBadge != null) {
            String growth = calculateTrend(lastDaySales, firstDaySales);
            growthBadge.setText(growth + " vs 7 hari lalu");
            if (growth.startsWith("+")) {
                growthBadge.setStyle(
                        "-fx-background-color: #EFF6FF; -fx-text-fill: #1E40AF; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 6 12; -fx-background-radius: 20;");
            } else {
                growthBadge.setStyle(
                        "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 6 12; -fx-background-radius: 20;");
            }
        }
    }

    private void loadLatestTransactions() {
        if (latestTransactionsContainer == null) {
            return;
        }

        latestTransactionsContainer.getChildren().clear();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT invoice_number, sale_date, total_amount, status FROM sales ORDER BY sale_date DESC LIMIT 5");
                ResultSet rs = stmt.executeQuery()) {

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                latestTransactionsContainer.getChildren().add(createTransactionRow(
                        rs.getString("invoice_number"),
                        formatDateTime(rs.getString("sale_date")),
                        BigDecimal.valueOf(rs.getLong("total_amount")),
                        rs.getString("status")));
            }

            if (!hasRows) {
                Label empty = new Label("Belum ada transaksi.");
                empty.getStyleClass().add("mini-td");
                latestTransactionsContainer.getChildren().add(empty);
            }
        } catch (Exception e) {
            logger.error("Error loading latest transactions", e);
            Label error = new Label("Gagal memuat transaksi terbaru.");
            error.getStyleClass().add("mini-td");
            latestTransactionsContainer.getChildren().add(error);
        }
    }

    private HBox createTransactionRow(String invoiceNumber, String saleDate, BigDecimal totalAmount, String status) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("list-row");

        VBox details = new VBox(2);
        details.getStyleClass().add("transaction-details");
        HBox.setHgrow(details, Priority.ALWAYS);

        Label invoice = new Label(invoiceNumber == null || invoiceNumber.isBlank() ? "-" : invoiceNumber);
        invoice.getStyleClass().add("row-title");

        Label time = new Label(saleDate);
        time.getStyleClass().add("row-subtitle");
        details.getChildren().addAll(invoice, time);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label total = new Label(currencyFormat.format(totalAmount == null ? BigDecimal.ZERO : totalAmount));
        total.getStyleClass().add("row-value");

        Label statusLabel = new Label(normalizeStatus(status));
        statusLabel.getStyleClass().add("mini-td");
        statusLabel.getStyleClass().add("COMPLETED".equalsIgnoreCase(status) ? "status-success" : "status-pending");

        row.getChildren().addAll(details, spacer, total, statusLabel);
        return row;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "-";
        }
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "COMPLETED" -> "BERHASIL";
            case "VOIDED" -> "BATAL";
            case "RETURNED" -> "RETUR";
            default -> status.toUpperCase(Locale.ROOT);
        };
    }

    private String formatDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return "-";
        }
        String normalized = raw.replace('T', ' ');
        if (normalized.length() >= 19) {
            return normalized.substring(0, 19);
        }
        return normalized;
    }

    private void loadTopProducts() {
        try {
            LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
            LocalDate monthEnd = LocalDate.now();

            // Get all sales data
            var allSales = reportDAO.getSalesReportByProductType(monthStart, monthEnd, "LAPTOP_NEW");
            allSales.addAll(reportDAO.getSalesReportByProductType(monthStart, monthEnd, "LAPTOP_SECOND"));
            allSales.addAll(reportDAO.getSalesReportByProductType(monthStart, monthEnd, "PERIPHERAL"));
            allSales.addAll(reportDAO.getSalesReportByProductType(monthStart, monthEnd, "SERVICE"));

            // Sort by quantity sold
            allSales.sort((a, b) -> Integer.compare(b.getInt("qty_sold"), a.getInt("qty_sold")));

            // Top 3
            if (allSales.size() > 0) {
                ReportDAO.ReportRow p1 = allSales.get(0);
                if (topProduct1Name != null)
                    topProduct1Name.setText(truncate(p1.getString("product_name"), 25));
                if (topProduct1Sku != null)
                    topProduct1Sku.setText("SKU: " + p1.getString("sku"));
                if (topProduct1Qty != null)
                    topProduct1Qty.setText(p1.getInt("qty_sold") + " Terjual");
            } else {
                if (topProduct1Name != null)
                    topProduct1Name.setText("Belum ada data");
                if (topProduct1Sku != null)
                    topProduct1Sku.setText("SKU: -");
                if (topProduct1Qty != null)
                    topProduct1Qty.setText("-");
            }

            if (allSales.size() > 1) {
                ReportDAO.ReportRow p2 = allSales.get(1);
                if (topProduct2Name != null)
                    topProduct2Name.setText(truncate(p2.getString("product_name"), 25));
                if (topProduct2Sku != null)
                    topProduct2Sku.setText("SKU: " + p2.getString("sku"));
                if (topProduct2Qty != null)
                    topProduct2Qty.setText(p2.getInt("qty_sold") + " Terjual");
            } else {
                if (topProduct2Name != null)
                    topProduct2Name.setText("Belum ada data");
                if (topProduct2Sku != null)
                    topProduct2Sku.setText("SKU: -");
                if (topProduct2Qty != null)
                    topProduct2Qty.setText("-");
            }

            if (allSales.size() > 2) {
                ReportDAO.ReportRow p3 = allSales.get(2);
                if (topProduct3Name != null)
                    topProduct3Name.setText(truncate(p3.getString("product_name"), 25));
                if (topProduct3Sku != null)
                    topProduct3Sku.setText("SKU: " + p3.getString("sku"));
                if (topProduct3Qty != null)
                    topProduct3Qty.setText(p3.getInt("qty_sold") + " Terjual");
            } else {
                if (topProduct3Name != null)
                    topProduct3Name.setText("Belum ada data");
                if (topProduct3Sku != null)
                    topProduct3Sku.setText("SKU: -");
                if (topProduct3Qty != null)
                    topProduct3Qty.setText("-");
            }

        } catch (Exception e) {
            logger.error("Error loading top products", e);
        }
    }

    private void loadLowStockProducts() {
        try {
            String countSql = "SELECT COUNT(*) FROM products WHERE is_active = 1 AND stock <= 5";
            String sql = "SELECT name, stock FROM products WHERE is_active = 1 AND stock <= 5 ORDER BY stock ASC LIMIT 3";

            List<String[]> lowStockList = new ArrayList<>();

            try (Connection conn = DatabaseConfig.getConnection();
                    PreparedStatement countStmt = conn.prepareStatement(countSql);
                    ResultSet countRs = countStmt.executeQuery();
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery()) {

                if (countRs.next() && lowStockTotalLabel != null) {
                    int count = countRs.getInt(1);
                    lowStockTotalLabel.setText(count + " item");
                }

                while (rs.next()) {
                    lowStockList.add(new String[] { rs.getString("name"), String.valueOf(rs.getInt("stock")) });
                }
            }

            // Update labels
            if (lowStockList.size() > 0) {
                String[] item1 = lowStockList.get(0);
                if (lowStock1Name != null)
                    lowStock1Name.setText(truncate(item1[0], 22));
                if (lowStock1Qty != null)
                    lowStock1Qty.setText("Sisa " + item1[1] + " unit");
            } else {
                if (lowStock1Name != null)
                    lowStock1Name.setText("Stok aman");
                if (lowStock1Qty != null)
                    lowStock1Qty.setText("-");
            }

            if (lowStockList.size() > 1) {
                String[] item2 = lowStockList.get(1);
                if (lowStock2Name != null)
                    lowStock2Name.setText(truncate(item2[0], 22));
                if (lowStock2Qty != null)
                    lowStock2Qty.setText("Sisa " + item2[1] + " unit");
            } else {
                if (lowStock2Name != null)
                    lowStock2Name.setText("Stok aman");
                if (lowStock2Qty != null)
                    lowStock2Qty.setText("-");
            }

            if (lowStockList.size() > 2) {
                String[] item3 = lowStockList.get(2);
                if (lowStock3Name != null)
                    lowStock3Name.setText(truncate(item3[0], 22));
                if (lowStock3Qty != null)
                    lowStock3Qty.setText("Sisa " + item3[1] + " unit");
            } else {
                if (lowStock3Name != null)
                    lowStock3Name.setText("Stok aman");
                if (lowStock3Qty != null)
                    lowStock3Qty.setText("-");
            }

        } catch (Exception e) {
            logger.error("Error loading low stock products", e);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null)
            return "";
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Sets the parent dashboard controller for navigation purposes
     */
    public void setParentController(DashboardController parent) {
        this.parentController = parent;
    }
}


