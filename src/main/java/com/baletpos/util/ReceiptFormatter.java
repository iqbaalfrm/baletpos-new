package com.baletpos.util;

import com.baletpos.model.Sale;
import com.baletpos.model.SaleItem;
import com.baletpos.model.SalePayment;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ReceiptFormatter {

    // SPEC: 40-42 Characters (We use 42 for max usage of space)
    private static final int WIDTH = 42;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DecimalFormat CURRENCY_FMT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("id", "ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        CURRENCY_FMT = new DecimalFormat("#,###", symbols);
    }

    public static String format(Sale sale) {
        StringBuilder sb = new StringBuilder();

        // ==================================================
        // HEADER
        // ==================================================
        sb.append("BALET COMPUTER\n");
        sb.append("Jalan Prof. Moh. Yamin No 57\n");
        sb.append("Kudaile Slawi Kab. Tegal\n");
        sb.append(separator()).append("\n");

        sb.append(pair("No Nota", ": " + sale.getInvoiceNumber()));
        sb.append("\n");
        sb.append(pair("Tanggal", ": " + sale.getSaleDate().format(DATE_FMT)));
        sb.append("\n");

        String cashier = sale.getCreatedByName() != null ? sale.getCreatedByName() : "System";
        sb.append(pair("Kasir", ": " + cashier));
        sb.append("\n");

        String customer = sale.getCustomerName() != null ? sale.getCustomerName() : "Umum";
        sb.append(pair("Pelanggan", ": " + customer));
        sb.append("\n\n");
        sb.append(separator()).append("\n");

        // ==================================================
        // TABEL ITEM
        // ==================================================
        // Header Tabel
        sb.append("No  Kode        Nama Barang\n");
        sb.append("    Harga   Qty  Diskon  Subtotal\n");
        sb.append(separator()).append("\n");

        // Items
        // baris 1: 1 LPT-ASUS-01 ASUS VivoBook 14
        // baris 2: 8.400.000 1 0 8.400.000
        int no = 1;
        for (SaleItem item : sale.getItems()) {
            // Line 1: No + SKU + Name
            String noStr = padRight(String.valueOf(no), 3);
            // Ambil SKU (coba pangkas jika terlalu panjang, 11 chars)
            String sku = item.getProductSku();
            if (sku.length() > 11)
                sku = sku.substring(0, 11);
            else
                sku = padRight(sku, 11);

            String name = item.getProductName();
            // Sisa lebar untuk nama = 42 - 3 - 1 - 11 - 1 = 26 chars
            // Tapi boleh wrap atau cut. Di spec "Nama barang boleh dipotong baris"
            // Kita potong saja agar rapi 1 baris di line 1, atau biarkan panjang (wrap)
            // Untuk kerapian dot matrix biasanya di-cut.
            int maxName = WIDTH - 16;
            if (name.length() > maxName)
                name = name.substring(0, maxName);

            sb.append(noStr).append(" ").append(sku).append(" ").append(name).append("\n");

            // Line 2: Prices
            // Format: Harga Qty Diskon Subtotal
            // Spec: Angka RATA KANAN
            String price = formatMoney(item.getUnitPrice());
            String qty = String.valueOf(item.getQuantity());
            String disc = formatMoney(item.getDiscountAmount());
            // Jika item discount = 0, tampilkan 0
            if (item.getDiscountAmount().compareTo(BigDecimal.ZERO) == 0)
                disc = "0";

            String subtotal = formatMoney(item.getSubtotal());

            // Build aligned string manually
            // Layout target (approx):
            // ....8.400.000..1....0....8.400.000
            // Columns widths logic:
            // Indent 4 (bawah "No ")
            // Price: 11 chars (cukup utk puluhan juta)
            // Qty: 4 chars
            // Disc: 9 chars
            // Subtotal: 13 chars (WIDTH - 4 - 11 - 4 - 9 = 14)

            String sPrice = padLeft(price, 11);
            String sQty = padLeft(qty, 4);
            String sDisc = padLeft(disc, 9);
            String sSub = padLeft(subtotal, 13); // sisa lebar

            sb.append("    ").append(sPrice).append(sQty).append(sDisc).append(sSub).append("\n");

            no++;
        }
        sb.append(separator()).append("\n");

        // ==================================================
        // RINGKASAN PEMBAYARAN
        // ==================================================

        // Total Harga & Diskon Global
        sb.append(pair("Total Harga", ": " + formatMoney(sale.getSubtotal())));
        sb.append("\n");
        sb.append(pair("Diskon", ": " + formatMoney(sale.getDiscountAmount())));
        sb.append("\n");
        sb.append(separator()).append("\n");

        sb.append(pair("TOTAL BAYAR", ": " + formatMoney(sale.getTotalAmount())));
        sb.append("\n\n");

        // Pembayaran
        if (sale.getPaymentType() == Sale.PaymentType.SPLIT && sale.getPayments() != null) {
            for (SalePayment p : sale.getPayments()) {
                String label = p.getMethod();
                if (label.contains("PAYLATER_"))
                    label = label.replace("PAYLATER_", "");
                // Clean up enum names
                if (label.equals("TRANSFER_BCA"))
                    label = "Transfer BCA";
                else if (label.equals("DEBIT_BRI"))
                    label = "Debit BRI";
                else if (label.equals("PENGADAAN_CV"))
                    label = "Pengadaan";

                sb.append(pair("Metode Bayar", ": " + label)).append("\n");
                sb.append(pair("Bayar", ": " + formatMoney(p.getAmount()))).append("\n");
            }
        } else {
            String method = "CASH";
            if (sale.getPaymentMethod() != null) {
                method = sale.getPaymentMethod().getDisplayName();
                // Fallback if displayName null
                if (method == null)
                    method = sale.getPaymentMethod().name();
            }
            sb.append(pair("Metode Bayar", ": " + method)).append("\n");
            sb.append(pair("Tunai", ": " + formatMoney(sale.getPaymentAmount()))).append("\n");
        }

        sb.append(pair("Kembali", ": " + formatMoney(sale.getChangeAmount())));
        sb.append("\n\n");
        sb.append(separator()).append("\n");

        // ==================================================
        // FOOTER
        // ==================================================
        sb.append(center("Barang yang sudah dibeli")).append("\n");
        sb.append(center("tidak dapat dikembalikan / ditukar")).append("\n\n");
        sb.append(center("*** TERIMA KASIH ATAS KUNJUNGAN ANDA ***")).append("\n");
        sb.append(separator()).append("\n");

        return sb.toString();
    }

    private static String separator() {
        return "-".repeat(WIDTH);
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null)
            return "0";
        return CURRENCY_FMT.format(amount);
    }

    // Helper: Center text
    private static String center(String s) {
        if (s.length() >= WIDTH)
            return s;
        int padding = (WIDTH - s.length()) / 2;
        return " ".repeat(padding) + s;
    }

    // Helper: Key-Value pair with alignment
    // Key (left) ...... Value (right) -> No, user spec is "No Nota : INV..." (Left
    // aligned keys)
    // Looking at spec:
    // No Nota : INV...
    // Tanggal : DD...
    // This is Fixed Width Columns for Key.
    private static String pair(String key, String value) {
        // Assume key width 12 chars
        String k = padRight(key, 12);
        return k + value;
    }

    // Helper: Pad Right (Text align Left)
    private static String padRight(String s, int n) {
        if (s.length() > n)
            return s;
        return String.format("%-" + n + "s", s);
    }

    // Helper: Pad Left (Number align Right)
    private static String padLeft(String s, int n) {
        if (s.length() > n)
            return s;
        return String.format("%" + n + "s", s);
    }
}


