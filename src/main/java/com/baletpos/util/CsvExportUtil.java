package com.baletpos.util;

import com.baletpos.dao.ReportDAO.ReportRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utility untuk export data ke CSV
 */
public class CsvExportUtil {
    private static final Logger logger = LoggerFactory.getLogger(CsvExportUtil.class);
    private static final String EXPORT_DIR = System.getProperty("user.home") + "/.baletpos/exports/";
    private static final DateTimeFormatter FILE_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    static {
        try {
            Files.createDirectories(Paths.get(EXPORT_DIR));
        } catch (IOException e) {
            logger.error("Failed to create export directory", e);
        }
    }

    /**
     * Export Category Sales Report to CSV
     */
    public static File exportCategorySalesReport(List<ReportRow> data, String category,
            LocalDate startDate, LocalDate endDate) throws IOException {

        String filename = String.format("Sales_%s_%s.csv", category, LocalDateTime.now().format(FILE_DATE_FMT));
        File file = new File(EXPORT_DIR + filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Header
            writer.println("Laporan Penjualan " + category);
            writer.println("Periode: " + startDate + " s/d " + endDate);
            writer.println();

            // Column Headers
            writer.println("SKU,Nama Produk,Qty Terjual,Omzet,HPP,Laba Kotor");

            // Data rows
            for (ReportRow row : data) {
                writer.printf("%s,%s,%d,%d,%d,%d%n",
                        escapeCSV(row.getString("sku")),
                        escapeCSV(row.getString("product_name")),
                        row.getInt("qty_sold"),
                        row.getLong("revenue"),
                        row.getLong("cogs"),
                        row.getLong("gross_profit"));
            }

            // Totals
            int totalQty = data.stream().mapToInt(r -> r.getInt("qty_sold")).sum();
            long totalRevenue = data.stream().mapToLong(r -> r.getLong("revenue")).sum();
            long totalCogs = data.stream().mapToLong(r -> r.getLong("cogs")).sum();
            long totalProfit = totalRevenue - totalCogs;

            writer.println();
            writer.printf("TOTAL,,%d,%d,%d,%d%n", totalQty, totalRevenue, totalCogs, totalProfit);
        }

        logger.info("CSV exported: {}", file.getAbsolutePath());
        return file;
    }

    /**
     * Export Inventory Valuation Report to CSV
     */
    public static File exportInventoryReport(List<ReportRow> data) throws IOException {
        String filename = String.format("Inventory_Aset_%s.csv", LocalDateTime.now().format(FILE_DATE_FMT));
        File file = new File(EXPORT_DIR + filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Laporan Aset / Inventory Valuation");
            writer.println("Tanggal: " + LocalDate.now());
            writer.println();

            writer.println("SKU,Nama Produk,Tipe,Stok,HPP/Unit,Harga Jual,Nilai HPP,Nilai Jual");

            long totalHppValue = 0;
            long totalSellValue = 0;

            for (ReportRow row : data) {
                int stock = row.getInt("stock");
                long hpp = row.getLong("hpp");
                long sellPrice = row.getLong("selling_price");
                long hppValue = stock * hpp;
                long sellValue = stock * sellPrice;

                totalHppValue += hppValue;
                totalSellValue += sellValue;

                writer.printf("%s,%s,%s,%d,%d,%d,%d,%d%n",
                        escapeCSV(row.getString("sku")),
                        escapeCSV(row.getString("name")),
                        escapeCSV(row.getString("product_type")),
                        stock,
                        hpp,
                        sellPrice,
                        hppValue,
                        sellValue);
            }

            writer.println();
            writer.printf("TOTAL,,,,,,,%d,%d%n", totalHppValue, totalSellValue);
        }

        logger.info("CSV exported: {}", file.getAbsolutePath());
        return file;
    }

    /**
     * Export Expense Report to CSV
     */
    public static File exportExpenseReport(List<ReportRow> data, LocalDate startDate, LocalDate endDate)
            throws IOException {
        String filename = String.format("Biaya_%s.csv", LocalDateTime.now().format(FILE_DATE_FMT));
        File file = new File(EXPORT_DIR + filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Laporan Biaya Operasional");
            writer.println("Periode: " + startDate + " s/d " + endDate);
            writer.println();

            writer.println("Kode Biaya,Nama Biaya,Jumlah Transaksi,Total Nominal");

            long grandTotal = 0;

            for (ReportRow row : data) {
                long total = row.getLong("total_amount");
                grandTotal += total;

                writer.printf("%s,%s,%d,%d%n",
                        escapeCSV(row.getString("expense_code")),
                        escapeCSV(row.getString("expense_name")),
                        row.getInt("transaction_count"),
                        total);
            }

            writer.println();
            writer.printf("GRAND TOTAL,,,%d%n", grandTotal);
        }

        logger.info("CSV exported: {}", file.getAbsolutePath());
        return file;
    }

    /**
     * Export Profit Loss Report to CSV
     */
    public static File exportProfitLossReport(Map<String, Object> data, LocalDate startDate, LocalDate endDate)
            throws IOException {
        String filename = String.format("LabaRugi_%s.csv", LocalDateTime.now().format(FILE_DATE_FMT));
        File file = new File(EXPORT_DIR + filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Laporan Laba Rugi");
            writer.println("Periode: " + startDate + " s/d " + endDate);
            writer.println();

            writer.println("Komponen,Nilai");
            writer.printf("Penjualan Kotor,%d%n", getLong(data, "gross_revenue"));
            writer.printf("Retur Penjualan,%d%n", getLong(data, "sales_return"));
            writer.printf("Penjualan Bersih,%d%n", getLong(data, "net_revenue"));
            writer.println();
            writer.printf("HPP Penjualan,%d%n", getLong(data, "cogs"));
            writer.printf("HPP Retur,%d%n", getLong(data, "return_cogs"));
            writer.printf("HPP Bersih,%d%n", getLong(data, "net_cogs"));
            writer.println();
            writer.printf("Laba Kotor,%d%n", getLong(data, "gross_profit"));
            writer.println();
            writer.printf("Total Biaya Operasional,%d%n", getLong(data, "total_expenses"));
            writer.println();
            writer.printf("Laba Bersih,%d%n", getLong(data, "net_profit"));
        }

        logger.info("CSV exported: {}", file.getAbsolutePath());
        return file;
    }

    private static long getLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Long)
            return (Long) val;
        if (val instanceof Integer)
            return ((Integer) val).longValue();
        if (val instanceof Number)
            return ((Number) val).longValue();
        return 0L;
    }

    private static String escapeCSV(String value) {
        if (value == null)
            return "";
        // Escape double quotes and wrap in quotes if contains comma or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}


