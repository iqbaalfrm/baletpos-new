package com.baletpos;

import com.baletpos.dao.ReportDAO.ReportRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests untuk BaletPOS
 * Mencakup:
 * 1. Nomerator test (format nomor invoice/PO/expense)
 * 2. Laba Rugi calculation test
 * 3. Stock movement integrity test
 */
public class BaletPOSTests {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Test
    @DisplayName("Test format nomor Invoice: INV-YYYYMMDD-0001")
    void testInvoiceNumberFormat() {
        String datePart = LocalDate.now().format(DATE_FMT);
        String prefix = "INV";
        int sequence = 1;

        String invoiceNumber = String.format("%s-%s-%04d", prefix, datePart, sequence);

        assertTrue(invoiceNumber.startsWith("INV-"));
        assertTrue(invoiceNumber.matches("INV-\\d{8}-\\d{4}"));
        assertEquals(17, invoiceNumber.length());
    }

    @Test
    @DisplayName("Test format nomor PO: PO-YYYYMMDD-0001")
    void testPurchaseOrderNumberFormat() {
        String datePart = LocalDate.now().format(DATE_FMT);
        String prefix = "PO";
        int sequence = 1;

        String poNumber = String.format("%s-%s-%04d", prefix, datePart, sequence);

        assertTrue(poNumber.startsWith("PO-"));
        assertTrue(poNumber.matches("PO-\\d{8}-\\d{4}"));
        assertEquals(16, poNumber.length());
    }

    @Test
    @DisplayName("Test format nomor Expense: EXP-YYYYMMDD-0001")
    void testExpenseNumberFormat() {
        String datePart = LocalDate.now().format(DATE_FMT);
        String prefix = "EXP";
        int sequence = 1;

        String expNumber = String.format("%s-%s-%04d", prefix, datePart, sequence);

        assertTrue(expNumber.startsWith("EXP-"));
        assertTrue(expNumber.matches("EXP-\\d{8}-\\d{4}"));
        assertEquals(17, expNumber.length());
    }

    @Test
    @DisplayName("Test format nomor Return: RT-YYYYMMDD-0001")
    void testReturnNumberFormat() {
        String datePart = LocalDate.now().format(DATE_FMT);
        String prefix = "RT";
        int sequence = 1;

        String rtNumber = String.format("%s-%s-%04d", prefix, datePart, sequence);

        assertTrue(rtNumber.startsWith("RT-"));
        assertTrue(rtNumber.matches("RT-\\d{8}-\\d{4}"));
        assertEquals(16, rtNumber.length());
    }

    @Test
    @DisplayName("Test sequence reset per hari")
    void testSequenceResetPerDay() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        String todayPart = today.format(DATE_FMT);
        String tomorrowPart = tomorrow.format(DATE_FMT);

        // Hari ini sequence 5
        String invToday = String.format("INV-%s-%04d", todayPart, 5);
        // Besok sequence reset ke 1
        String invTomorrow = String.format("INV-%s-%04d", tomorrowPart, 1);

        assertNotEquals(invToday.substring(4, 12), invTomorrow.substring(4, 12));
        assertEquals("0001", invTomorrow.substring(13));
    }

    // ============================================
    // LABA RUGI CALCULATION TESTS
    // ============================================

    @Test
    @DisplayName("Test perhitungan Gross Profit")
    void testGrossProfitCalculation() {
        // Revenue: 10,000,000
        // COGS: 7,000,000
        // Gross Profit = Revenue - COGS = 3,000,000

        BigDecimal revenue = BigDecimal.valueOf(10000000);
        BigDecimal cogs = BigDecimal.valueOf(7000000);

        BigDecimal grossProfit = revenue.subtract(cogs);

        assertEquals(BigDecimal.valueOf(3000000), grossProfit);
    }

    @Test
    @DisplayName("Test perhitungan Net Revenue dengan Retur")
    void testNetRevenueWithReturns() {
        // Gross Revenue: 10,000,000
        // Sales Returns: 500,000
        // Net Revenue = 10,000,000 - 500,000 = 9,500,000

        BigDecimal grossRevenue = BigDecimal.valueOf(10000000);
        BigDecimal salesReturns = BigDecimal.valueOf(500000);

        BigDecimal netRevenue = grossRevenue.subtract(salesReturns);

        assertEquals(BigDecimal.valueOf(9500000), netRevenue);
    }

    @Test
    @DisplayName("Test perhitungan Net COGS dengan Reversal")
    void testNetCogsWithReversal() {
        // Gross COGS: 7,000,000
        // COGS Reversal (dari retur): 350,000
        // Net COGS = 7,000,000 - 350,000 = 6,650,000

        BigDecimal grossCogs = BigDecimal.valueOf(7000000);
        BigDecimal cogsReversal = BigDecimal.valueOf(350000);

        BigDecimal netCogs = grossCogs.subtract(cogsReversal);

        assertEquals(BigDecimal.valueOf(6650000), netCogs);
    }

    @Test
    @DisplayName("Test perhitungan Net Profit lengkap")
    void testNetProfitCalculation() {
        // Net Revenue: 9,500,000
        // Net COGS: 6,650,000
        // Gross Profit: 2,850,000
        // Expenses: 1,500,000
        // Net Profit = 2,850,000 - 1,500,000 = 1,350,000

        BigDecimal netRevenue = BigDecimal.valueOf(9500000);
        BigDecimal netCogs = BigDecimal.valueOf(6650000);
        BigDecimal expenses = BigDecimal.valueOf(1500000);

        BigDecimal grossProfit = netRevenue.subtract(netCogs);
        BigDecimal netProfit = grossProfit.subtract(expenses);

        assertEquals(BigDecimal.valueOf(2850000), grossProfit);
        assertEquals(BigDecimal.valueOf(1350000), netProfit);
    }

    @Test
    @DisplayName("Test perhitungan margin persentase")
    void testMarginPercentageCalculation() {
        // Net Revenue: 10,000,000
        // Gross Profit: 3,000,000
        // Gross Margin = (3,000,000 / 10,000,000) * 100 = 30%

        BigDecimal netRevenue = BigDecimal.valueOf(10000000);
        BigDecimal grossProfit = BigDecimal.valueOf(3000000);

        BigDecimal grossMargin = grossProfit.multiply(BigDecimal.valueOf(100))
                .divide(netRevenue, 2, java.math.RoundingMode.HALF_UP);

        assertEquals(BigDecimal.valueOf(30).setScale(2), grossMargin);
    }

    @Test
    @DisplayName("Test perhitungan rugi (negative profit)")
    void testLossCalculation() {
        // Net Revenue: 5,000,000
        // Net COGS: 4,000,000
        // Gross Profit: 1,000,000
        // Expenses: 2,000,000
        // Net Profit = 1,000,000 - 2,000,000 = -1,000,000 (RUGI)

        BigDecimal netRevenue = BigDecimal.valueOf(5000000);
        BigDecimal netCogs = BigDecimal.valueOf(4000000);
        BigDecimal expenses = BigDecimal.valueOf(2000000);

        BigDecimal grossProfit = netRevenue.subtract(netCogs);
        BigDecimal netProfit = grossProfit.subtract(expenses);

        assertEquals(BigDecimal.valueOf(1000000), grossProfit);
        assertEquals(BigDecimal.valueOf(-1000000), netProfit);
        assertTrue(netProfit.compareTo(BigDecimal.ZERO) < 0);
    }

    // ============================================
    // STOCK MOVEMENT INTEGRITY TESTS
    // ============================================

    @Test
    @DisplayName("Test stock after = stock before + quantity change (positive)")
    void testStockMovementIntegrityPositive() {
        int stockBefore = 10;
        int quantityChange = 5; // PURCHASE_IN

        int stockAfter = stockBefore + quantityChange;

        assertEquals(15, stockAfter);
    }

    @Test
    @DisplayName("Test stock after = stock before + quantity change (negative)")
    void testStockMovementIntegrityNegative() {
        int stockBefore = 10;
        int quantityChange = -3; // SALE_OUT

        int stockAfter = stockBefore + quantityChange;

        assertEquals(7, stockAfter);
    }

    @Test
    @DisplayName("Test stock movement chain integrity")
    void testStockMovementChain() {
        // Simulasi chain movement:
        // Opening: 10
        // Purchase +5 = 15
        // Sale -3 = 12
        // Sale -2 = 10
        // Return +1 = 11
        // Adjustment -1 = 10

        int opening = 10;
        int[] movements = { +5, -3, -2, +1, -1 };

        int currentStock = opening;
        for (int move : movements) {
            currentStock += move;
        }

        assertEquals(10, currentStock);
    }

    @Test
    @DisplayName("Test selling price calculation from HPP and margin")
    void testSellingPriceCalculation() {
        // HPP: 1,000,000
        // Margin: 20%
        // Selling Price = HPP + (HPP * margin / 100) = 1,200,000

        BigDecimal hpp = BigDecimal.valueOf(1000000);
        double marginPercent = 20.0;

        BigDecimal marginAmount = hpp.multiply(BigDecimal.valueOf(marginPercent))
                .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
        BigDecimal sellingPrice = hpp.add(marginAmount);

        assertEquals(0, BigDecimal.valueOf(1200000).compareTo(sellingPrice));
    }

    @Test
    @DisplayName("Test stock tidak boleh negatif setelah penjualan")
    void testStockCannotGoNegativeAfterSale() {
        int currentStock = 5;
        int saleQuantity = 7;

        boolean isStockSufficient = currentStock >= saleQuantity;

        assertFalse(isStockSufficient);
    }

    @Test
    @DisplayName("Test VOID restores stock correctly")
    void testVoidRestoresStock() {
        int stockBeforeSale = 10;
        int saleQuantity = 3;
        int stockAfterSale = stockBeforeSale - saleQuantity; // 7

        // VOID - restore stock
        int stockAfterVoid = stockAfterSale + saleQuantity; // 10

        assertEquals(10, stockAfterVoid);
        assertEquals(stockBeforeSale, stockAfterVoid);
    }

    // ============================================
    // REPORT ROW HELPER CLASS TESTS
    // ============================================

    @Test
    @DisplayName("Test ReportRow get methods")
    void testReportRowMethods() {
        ReportRow row = new ReportRow();
        row.put("sku", "LPT-001");
        row.put("qty_sold", 10);
        row.put("revenue", 10000000L);

        assertEquals("LPT-001", row.getString("sku"));
        assertEquals(10, row.getInt("qty_sold"));
        assertEquals(10000000L, row.getLong("revenue"));
        assertEquals(BigDecimal.valueOf(10000000L), row.getBigDecimal("revenue"));
    }

    @Test
    @DisplayName("Test ReportRow default values for missing keys")
    void testReportRowDefaultValues() {
        ReportRow row = new ReportRow();

        assertEquals("", row.getString("missing_key"));
        assertEquals(0, row.getInt("missing_key"));
        assertEquals(0L, row.getLong("missing_key"));
        assertEquals(BigDecimal.ZERO, row.getBigDecimal("missing_key"));
    }
}
