package com.baletpos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DemoDataSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final Random random = new Random();
    private static final DateTimeFormatter DB_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void seed(Connection conn) {
        try {
            conn.setAutoCommit(false);

            logger.info("Starting DEMO DATA seeding...");

            // 1. Seed Categories & Brands
            seedCategoriesAndBrands(conn);

            // 2. Seed Suppliers
            seedSuppliers(conn);

            // 3. Seed Products (Laptop, Peripheral, Service)
            seedProducts(conn);

            // 4. Seed Customers
            seedCustomersNew(conn);

            // 5. Seed Purchases (Initial Stock)
            seedPurchases(conn);

            // 6. Seed Expenses (Operational Costs)
            seedExpenses(conn);

            // 7. Seed Sales (Transactions)
            seedSales(conn);

            // 8. Seed Sales Returns
            seedSalesReturns(conn);

            // 9. Seed Stock Adjustments
            seedStockAdjustments(conn);

            conn.commit();
            conn.setAutoCommit(true);
            logger.info("DEMO DATA seeding completed successfully!");

        } catch (Exception e) {
            logger.error("Failed to seed demo data", e);
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void seedCategoriesAndBrands(Connection conn) throws SQLException {
        String[] cats = {
                "LAPTOP,Laptop & Notebook,1",
                "PERIPHERAL,Aksesoris Komputer,1",
                "SERVICE,Jasa Service,1",
                "SPAREPART,Sparepart Laptop,1"
        };
        try (PreparedStatement ps = conn
                .prepareStatement("INSERT OR IGNORE INTO categories (code, name, is_active) VALUES (?, ?, ?)")) {
            for (String c : cats) {
                String[] parts = c.split(",");
                ps.setString(1, parts[0]);
                ps.setString(2, parts[1]);
                ps.setInt(3, Integer.parseInt(parts[2]));
                ps.addBatch();
            }
            ps.executeBatch();
        }

        String[] brands = { "ASUS", "LENOVO", "HP", "ACER", "LOGITECH", "SAMSUNG", "APPLE", "OTHER" };
        try (PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO brands (code, name) VALUES (?, ?)")) {
            for (String b : brands) {
                ps.setString(1, b);
                ps.setString(2, b);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedSuppliers(Connection conn) throws SQLException {
        String[][] suppliers = {
                { "SUP001", "PT. Synnex Metrodata", "Budi Santoso", "Jakarta Pusat", "021-1234567",
                        "sales@synnex.co.id" },
                { "SUP002", "PT. Dragon Computer", "Andi Wijaya", "Surabaya", "031-9876543", "dragon@comp.com" },
                { "SUP003", "CV. Juara Aksesoris", "Siti Aminah", "Bandung", "022-5555555", "juara@acc.com" },
                { "SUP004", "PT. Nusantara Digital", "Rizky Pratama", "Semarang", "024-7778888",
                        "sales@nusadigi.id" },
                { "SUP005", "CV. Mitra Komputer", "Dewi Lestari", "Yogyakarta", "0274-888999",
                        "mitra@komp.id" }
        };

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO suppliers (code, name, contact, address, phone, email) VALUES (?, ?, ?, ?, ?, ?)")) {
            for (String[] s : suppliers) {
                ps.setString(1, s[0]);
                ps.setString(2, s[1]);
                ps.setString(3, s[2]);
                ps.setString(4, s[3]);
                ps.setString(5, s[4]);
                ps.setString(6, s[5]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedProducts(Connection conn) throws SQLException {
        // Laptops
        createProduct(conn, "LPT-001", "ASUS ROG Strix G15", "LAPTOP_NEW", 1, 1, 15000000, 18500000, 5);
        createProduct(conn, "LPT-002", "Lenovo Legion 5", "LAPTOP_NEW", 1, 2, 14000000, 17200000, 5);
        createProduct(conn, "LPT-003", "MacBook Air M1", "LAPTOP_NEW", 1, 7, 11000000, 13500000, 10);
        createProduct(conn, "LPT-004", "HP Pavilion 14", "LAPTOP_NEW", 1, 3, 7000000, 8900000, 8);
        createProduct(conn, "LPT-005", "Acer Swift 3", "LAPTOP_NEW", 1, 4, 8500000, 10500000, 6);
        createProduct(conn, "LPT-006", "ASUS VivoBook 14", "LAPTOP_NEW", 1, 1, 6500000, 8200000, 7);
        createProduct(conn, "LPT-007", "Lenovo IdeaPad Slim 3", "LAPTOP_NEW", 1, 2, 6200000, 7900000, 8);
        createProduct(conn, "LPT-008", "HP Victus 16", "LAPTOP_NEW", 1, 3, 12500000, 15200000, 4);

        // Laptop Second
        createProduct(conn, "SEC-001", "ThinkPad T480 (Used)", "LAPTOP_SECOND", 1, 2, 4500000, 5500000, 3);
        createProduct(conn, "SEC-002", "Dell Latitude 5490 (Used)", "LAPTOP_SECOND", 1, 4, 4200000, 5200000, 2);
        createProduct(conn, "SEC-003", "HP EliteBook 840 G5 (Used)", "LAPTOP_SECOND", 1, 3, 4800000, 5900000, 4);
        createProduct(conn, "SEC-004", "Acer TravelMate P2 (Used)", "LAPTOP_SECOND", 1, 4, 3800000, 4700000, 3);
        createProduct(conn, "SEC-005", "ASUS ProBook (Used)", "LAPTOP_SECOND", 1, 1, 3600000, 4500000, 2);

        // Spareparts
        createProduct(conn, "SPR-001", "LED 14.0 Slim 30 Pin", "SPAREPARTS", 4, 8, 650000, 950000, 10);
        createProduct(conn, "SPR-002", "Keyboard ASUS X441", "SPAREPARTS", 4, 1, 150000, 250000, 5);
        createProduct(conn, "SPR-003", "Battery Lenovo T480", "SPAREPARTS", 4, 2, 450000, 750000, 3);
        createProduct(conn, "SPR-004", "RAM 8GB DDR4 SODIMM", "SPAREPARTS", 4, 9, 350000, 550000, 15);
        createProduct(conn, "SPR-005", "SSD NVMe 512GB", "SPAREPARTS", 4, 9, 550000, 780000, 12);
        createProduct(conn, "SPR-006", "Fan Cooler Laptop", "SPAREPARTS", 4, 8, 120000, 200000, 20);

        // Peripherals
        createProduct(conn, "ACC-001", "Logitech G102 Mouse", "PERIPHERAL", 2, 5, 180000, 250000, 20);
        createProduct(conn, "ACC-002", "Mechanical Keyboard RGB", "PERIPHERAL", 2, 8, 350000, 550000, 15);
        createProduct(conn, "ACC-003", "Mousepad Gaming XL", "PERIPHERAL", 2, 8, 50000, 95000, 50);
        createProduct(conn, "ACC-004", "Headset Gaming 7.1", "PERIPHERAL", 2, 8, 250000, 450000, 10);
        createProduct(conn, "ACC-005", "USB Hub 3.0", "PERIPHERAL", 2, 8, 75000, 125000, 30);
        createProduct(conn, "ACC-006", "Webcam HD 1080p", "PERIPHERAL", 2, 8, 180000, 280000, 12);
        createProduct(conn, "ACC-007", "Speaker Mini USB", "PERIPHERAL", 2, 8, 60000, 110000, 25);
        createProduct(conn, "ACC-008", "Cooling Pad Laptop", "PERIPHERAL", 2, 8, 90000, 160000, 18);

        // Services (No Stock)
        createProduct(conn, "SRV-001", "Install Ulang Windows", "SERVICE", 3, 8, 0, 150000, 999);
        createProduct(conn, "SRV-002", "Cleaning Laptop/PC", "SERVICE", 3, 8, 0, 100000, 999);
        createProduct(conn, "SRV-003", "Ganti LCD Laptop", "SERVICE", 3, 8, 0, 250000, 999);
        createProduct(conn, "SRV-004", "Upgrade SSD + Install", "SERVICE", 3, 8, 0, 200000, 999);
        createProduct(conn, "SRV-005", "Install Office Original", "SERVICE", 3, 8, 0, 300000, 999);
    }

    private static void createProduct(Connection conn, String sku, String name, String type, int catId, int brandId,
            int hpp, int sell, int stock) throws SQLException {
        // Check exist
        try (PreparedStatement check = conn.prepareStatement("SELECT count(*) FROM products WHERE sku = ?")) {
            check.setString(1, sku);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                return;
        }

        String sql = "INSERT INTO products (sku, name, product_type_code, category_id, brand_id, hpp, selling_price, stock, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setInt(4, catId);
            ps.setInt(5, brandId);
            ps.setInt(6, hpp);
            ps.setInt(7, sell);
            ps.setInt(8, stock); // Initial stock set directly, but also need movement entry
            ps.executeUpdate();

            // Add initial movement if stock > 0
            if (stock > 0 && !"SERVICE".equals(type)) {
                // Get ID
                int pId = -1;
                try (Statement st = conn.createStatement();
                        ResultSet r = st.executeQuery("SELECT last_insert_rowid()")) {
                    if (r.next())
                        pId = r.getInt(1);
                }

                if (pId != -1) {
                    addStockMovement(conn, pId, "INITIAL", null, null, stock, 0, stock, "Demo Initial Stock", 1);
                }
            }
        }
    }

    // Helper to add stock movement
    private static void addStockMovement(Connection conn, int productId, String type, String refType, Integer refId,
            int change, int before, int after, String notes, int userId) throws SQLException {
        String sql = "INSERT INTO stock_movements (product_id, movement_type, reference_type, reference_id, quantity_change, stock_before, stock_after, notes, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, type);
            ps.setString(3, refType);
            if (refId != null)
                ps.setInt(4, refId);
            else
                ps.setNull(4, java.sql.Types.INTEGER);
            ps.setInt(5, change);
            ps.setInt(6, before);
            ps.setInt(7, after);
            ps.setString(8, notes);
            ps.setInt(9, userId);
            ps.setString(10, LocalDateTime.now().format(DB_DATE_FMT));
            ps.executeUpdate();
        }
    }

    private static void seedCustomers(Connection conn) throws SQLException {
        String[] custs = { "Walk-in Customer", "Budi Santoso", "CV. Teknologi Maju", "Ahmad Dhani", "Siti Nurhaliza" };
        try (PreparedStatement ps = conn
                .prepareStatement("INSERT OR IGNORE INTO customers (name, is_active) VALUES (?, 1)")) {
            for (String c : custs) {
                ps.setString(1, c);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedPurchases(Connection conn) throws SQLException {
        String sql = "INSERT INTO purchases (purchase_number, supplier_id, purchase_date, total_amount, status, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 10; i++) {
                ps.setString(1, "PO-DEMO-" + String.format("%03d", i));
                ps.setInt(2, (i % 5) + 1);
                ps.setString(3, LocalDateTime.now().minusDays(35 - i).format(DB_DATE_FMT));
                ps.setInt(4, 8000000 + (i * 1250000));
                ps.setString(5, "COMPLETED");
                ps.setInt(6, 1);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedExpenses(Connection conn) throws SQLException {
        String[] codes = { "EXP-001", "EXP-002", "EXP-003", "EXP-004", "EXP-005", "EXP-006", "EXP-007",
                "EXP-008", "EXP-009", "EXP-010" };
        String[] names = { "Listrik", "Internet", "Gaji Karyawan", "Sewa Ruko", "Keamanan", "Air PDAM", "ATK",
                "Transportasi", "Perawatan", "Pajak" };

        try (PreparedStatement ps = conn
                .prepareStatement("INSERT OR IGNORE INTO expense_codes (code, name) VALUES (?, ?)")) {
            for (int i = 0; i < codes.length; i++) {
                ps.setString(1, codes[i]);
                ps.setString(2, names[i]);
                ps.addBatch();
            }
            ps.executeBatch();
        }

        // Insert Expenses
        String sql = "INSERT INTO expenses (expense_number, expense_code_id, expense_date, amount, description, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 10; i++) {
                ps.setString(1, "EXP-TRX-" + String.format("%03d", i));
                ps.setInt(2, i); // code id assumed sequence
                ps.setString(3, LocalDateTime.now().minusDays(30 - i).format(DB_DATE_FMT));
                ps.setInt(4, 250000 + (i * 175000));
                ps.setString(5, names[i - 1]);
                ps.setInt(6, 1);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedSales(Connection conn) throws SQLException {
        // Generate 30 transactions
        List<Integer> laptopIds = getProductIds(conn, "LAPTOP_NEW");
        List<Integer> accIds = getProductIds(conn, "PERIPHERAL");
        List<Integer> srvIds = getProductIds(conn, "SERVICE");

        for (int i = 1; i <= 30; i++) {
            LocalDateTime date = LocalDateTime.now().minusDays(random.nextInt(30)).minusHours(random.nextInt(10));
            String invoice = "INV-" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
                    + String.format("%03d", i);

            // Create Sale Header
            int customerId = 1; // Default to Walk-in (since we only seed 1 customer now)
            int technicianId = random.nextBoolean() ? (random.nextInt(2) + 1) : 0; // Random tech

            // Items
            List<SaleItemTemp> items = new ArrayList<>();
            // 70% chance buying laptop
            if (random.nextDouble() < 0.7 && !laptopIds.isEmpty()) {
                int pId = laptopIds.get(random.nextInt(laptopIds.size()));
                items.add(new SaleItemTemp(pId, 1));
            }
            // 60% chance buying peripheral
            if (random.nextDouble() < 0.6 && !accIds.isEmpty()) {
                int qty = random.nextInt(3) + 1;
                int pId = accIds.get(random.nextInt(accIds.size()));
                items.add(new SaleItemTemp(pId, qty));
            }
            // 40% chance service
            if (random.nextDouble() < 0.4 && !srvIds.isEmpty()) {
                int pId = srvIds.get(random.nextInt(srvIds.size()));
                items.add(new SaleItemTemp(pId, 1));
            }
            if (items.isEmpty() && !accIds.isEmpty()) {
                items.add(new SaleItemTemp(accIds.get(0), 1)); // Minimal 1 item
            }

            // Calculate totals
            long subtotal = 0;
            long totalHpp = 0;

            // We need to fetch prices
            for (SaleItemTemp item : items) {
                // Fetch price logic inline or pre-fetch?
                // For demo speed, query per item is fine
                try (PreparedStatement ps = conn
                        .prepareStatement("SELECT selling_price, hpp FROM products WHERE id = ?")) {
                    ps.setInt(1, item.productId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        item.unitPrice = rs.getInt("selling_price");
                        item.hpp = rs.getInt("hpp");
                        item.subtotal = item.unitPrice * item.qty;
                        subtotal += item.subtotal;
                        totalHpp += (long) item.hpp * item.qty;
                    }
                }
            }

            long totalAmount = subtotal; // No discount logic for simplicity

            // Payment Logic
            String paymentType = "SINGLE";
            String paymentMethod = "CASH"; // Default
            List<PaymentTemp> payments = new ArrayList<>();

            int payRand = i % 10; // Deterministic mix
            if (payRand == 0 || payRand == 1 || payRand == 2) { // 30% Cash
                paymentMethod = "CASH";
                payments.add(new PaymentTemp("CASH", totalAmount));
            } else if (payRand == 3 || payRand == 4) { // 20% Transfer BCA
                paymentMethod = "TRANSFER_BCA";
                payments.add(new PaymentTemp("TRANSFER_BCA", totalAmount));
            } else if (payRand == 5 || payRand == 6) { // 20% QRIS
                paymentMethod = "QRIS";
                payments.add(new PaymentTemp("QRIS", totalAmount));
            } else if (payRand == 7) { // 10% Split Cash + Transfer
                paymentType = "SPLIT";
                paymentMethod = "CASH";
                long p1 = totalAmount / 2;
                long p2 = totalAmount - p1;
                payments.add(new PaymentTemp("CASH", p1));
                payments.add(new PaymentTemp("TRANSFER_BCA", p2));
            } else if (payRand == 8) { // Split Cash + Paylater
                paymentType = "SPLIT";
                paymentMethod = "CASH";
                long p1 = totalAmount / 2;
                long p2 = totalAmount - p1;
                payments.add(new PaymentTemp("CASH", p1));
                payments.add(new PaymentTemp("AKULAKU", p2));
            } else if (payRand == 9) { // Kredivo (Paylater)
                paymentMethod = "KREDIVO";
                payments.add(new PaymentTemp("KREDIVO", totalAmount));
            }

            // Insert Sale
            String saleSql = "INSERT INTO sales (invoice_number, customer_id, sale_date, subtotal, total_amount, total_hpp, payment_method, payment_amount, change_amount, status, created_by, technician_id, payment_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int saleId = -1;
            try (PreparedStatement ps = conn.prepareStatement(saleSql)) {
                ps.setString(1, invoice);
                ps.setInt(2, customerId);
                ps.setString(3, date.format(DB_DATE_FMT));
                ps.setLong(4, subtotal);
                ps.setLong(5, totalAmount);
                ps.setLong(6, totalHpp);
                ps.setString(7, paymentMethod);
                ps.setLong(8, totalAmount); // Assuming full paid
                ps.setInt(9, 0);
                ps.setString(10, "COMPLETED");
                ps.setInt(11, 2); // Kasir ID
                if (technicianId > 0)
                    ps.setInt(12, technicianId);
                else
                    ps.setNull(12, Types.INTEGER);
                ps.setString(13, paymentType);
                ps.executeUpdate();

                try (Statement st = conn.createStatement();
                        ResultSet r = st.executeQuery("SELECT last_insert_rowid()")) {
                    if (r.next())
                        saleId = r.getInt(1);
                }
            }

            if (saleId != -1) {
                // Insert Items
                String itemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, hpp_per_unit, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                    for (SaleItemTemp item : items) {
                        ps.setInt(1, saleId);
                        ps.setInt(2, item.productId);
                        ps.setInt(3, item.qty);
                        ps.setInt(4, item.unitPrice);
                        ps.setInt(5, item.hpp);
                        ps.setLong(6, item.subtotal);
                        ps.addBatch();

                        // Update Stock & Movement
                        updateStock(conn, item.productId, -item.qty, saleId);
                    }
                    ps.executeBatch();
                }

                // Insert Payments
                String paySql = "INSERT INTO sale_payments (sale_id, method, amount, ref_no, created_at) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(paySql)) {
                    for (PaymentTemp p : payments) {
                        ps.setInt(1, saleId);
                        ps.setString(2, p.method);
                        ps.setLong(3, p.amount);
                        ps.setString(4, "REF-" + random.nextInt(9999));
                        ps.setString(5, date.format(DB_DATE_FMT));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    private static void updateStock(Connection conn, int prodId, int change, int saleId) throws SQLException {
        // Check current stock
        int current = 0;
        boolean isService = false;
        try (PreparedStatement ps = conn
                .prepareStatement("SELECT stock, product_type_code FROM products WHERE id = ?")) {
            ps.setInt(1, prodId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                current = rs.getInt("stock");
                if ("SERVICE".equals(rs.getString("product_type_code")))
                    isService = true;
            }
        }

        if (isService)
            return; // No stock for service

        int after = current + change;

        // Update product
        try (PreparedStatement ps = conn.prepareStatement("UPDATE products SET stock = ? WHERE id = ?")) {
            ps.setInt(1, after);
            ps.setInt(2, prodId);
            ps.executeUpdate();
        }

        // Add movement
        addStockMovement(conn, prodId, "SALE_OUT", "SALE", saleId, change, current, after, "Penjualan Demo", 2);
    }

    private static List<Integer> getProductIds(Connection conn, String type) throws SQLException {
        List<Integer> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM products WHERE product_type_code = ?")) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(rs.getInt(1));
        }
        return list;
    }

    private static class SaleItemTemp {
        int productId;
        int qty;
        int unitPrice;
        int hpp;
        long subtotal;

        SaleItemTemp(int pId, int q) {
            productId = pId;
            qty = q;
        }
    }

    private static class PaymentTemp {
        String method;
        long amount;

        PaymentTemp(String m, long a) {
            method = m;
            amount = a;
        }
    }

    /**
     * Seed sample sales returns (retur penjualan)
     */
    private static void seedSalesReturns(Connection conn) throws SQLException {
        logger.info("Seeding sales returns...");

        // Get some sale IDs to create returns for
        List<Integer> saleIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM sales WHERE status = 'COMPLETED' ORDER BY id LIMIT 10")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                saleIds.add(rs.getInt(1));
            }
        }

        if (saleIds.isEmpty()) {
            logger.info("No sales found to create returns");
            return;
        }

        String[] reasons = {
                "Barang cacat pabrik",
                "Tidak sesuai spek",
                "Pelanggan berubah pikiran",
                "Salah beli",
                "Produk rusak saat diterima"
        };

        // Create 5 sample returns
        for (int i = 0; i < Math.min(5, saleIds.size()); i++) {
            int saleId = saleIds.get(i);
            LocalDateTime returnDate = LocalDateTime.now().minusDays(random.nextInt(20));
            String returnNumber = "RTN-" + returnDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
                    + String.format("%03d", i + 1);

            // Get sale items for this sale
            List<int[]> saleItems = new ArrayList<>(); // [sale_item_id, product_id, qty, unit_price, hpp]
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, product_id, quantity, unit_price, hpp_per_unit FROM sale_items WHERE sale_id = ? LIMIT 1")) {
                ps.setInt(1, saleId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    saleItems.add(new int[] {
                            rs.getInt("id"),
                            rs.getInt("product_id"),
                            1, // Return only 1 qty
                            rs.getInt("unit_price"),
                            rs.getInt("hpp_per_unit")
                    });
                }
            }

            if (saleItems.isEmpty())
                continue;

            // Calculate total return amount
            long totalAmount = 0;
            for (int[] item : saleItems) {
                totalAmount += item[3] * item[2]; // unit_price * qty
            }

            // Insert sales_return header
            String insertReturn = "INSERT INTO sales_returns (return_number, sale_id, return_date, total_amount, reason, status, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
            int returnId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertReturn)) {
                ps.setString(1, returnNumber);
                ps.setInt(2, saleId);
                ps.setString(3, returnDate.format(DB_DATE_FMT));
                ps.setLong(4, totalAmount);
                ps.setString(5, reasons[i % reasons.length]);
                ps.setString(6, "COMPLETED");
                ps.setInt(7, 1); // Admin
                ps.executeUpdate();

                try (Statement st = conn.createStatement();
                        ResultSet r = st.executeQuery("SELECT last_insert_rowid()")) {
                    if (r.next())
                        returnId = r.getInt(1);
                }
            }

            if (returnId != -1) {
                // Insert return items
                String insertItem = "INSERT INTO sales_return_items (sales_return_id, sale_item_id, product_id, quantity, unit_price, hpp_per_unit, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                    for (int[] item : saleItems) {
                        ps.setInt(1, returnId);
                        ps.setInt(2, item[0]); // sale_item_id
                        ps.setInt(3, item[1]); // product_id
                        ps.setInt(4, item[2]); // qty
                        ps.setInt(5, item[3]); // unit_price
                        ps.setInt(6, item[4]); // hpp
                        ps.setLong(7, (long) item[3] * item[2]); // subtotal
                        ps.addBatch();

                        // Restore stock
                        try (PreparedStatement stockPs = conn.prepareStatement(
                                "UPDATE products SET stock = stock + ? WHERE id = ?")) {
                            stockPs.setInt(1, item[2]);
                            stockPs.setInt(2, item[1]);
                            stockPs.executeUpdate();
                        }
                    }
                    ps.executeBatch();
                }
            }
        }
        logger.info("Sales returns seeded successfully");
    }

    /**
     * Seed sample stock adjustments (mutasi stok)
     */
    private static void seedStockAdjustments(Connection conn) throws SQLException {
        logger.info("Seeding stock adjustments...");

        // Get some product IDs
        List<Integer> productIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM products WHERE product_type_code != 'SERVICE' AND is_active = 1 LIMIT 10")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                productIds.add(rs.getInt(1));
            }
        }

        if (productIds.isEmpty()) {
            logger.info("No products found for stock adjustments");
            return;
        }

        String[] reasons = { "RUSAK", "HILANG", "OPNAME", "LAINNYA" };
        String[] notes = {
                "Kondisi rusak saat pemeriksaan",
                "Tidak ditemukan saat stock opname",
                "Penyesuaian stok sistem",
                "Koreksi data",
                "Barang jatuh dan rusak"
        };

        // Create 5 sample adjustments
        for (int i = 0; i < Math.min(5, productIds.size()); i++) {
            int productId = productIds.get(i);
            LocalDateTime adjDate = LocalDateTime.now().minusDays(random.nextInt(25));
            String adjNumber = "ADJ-" + adjDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
                    + String.format("%03d", i + 1);

            // Get current stock
            int currentStock = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT stock FROM products WHERE id = ?")) {
                ps.setInt(1, productId);
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    currentStock = rs.getInt(1);
            }

            // Adjustment (mostly negative for realistic demo)
            int change = -(random.nextInt(2) + 1); // -1 or -2
            if (currentStock + change < 0)
                change = 0; // Don't go negative

            int afterStock = currentStock + change;

            // Insert stock_adjustment
            String insertAdj = "INSERT INTO stock_adjustments (adjustment_number, product_id, adjustment_date, quantity_change, reason, notes, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertAdj)) {
                ps.setString(1, adjNumber);
                ps.setInt(2, productId);
                ps.setString(3, adjDate.format(DB_DATE_FMT));
                ps.setInt(4, change);
                ps.setString(5, reasons[i % reasons.length]);
                ps.setString(6, notes[i % notes.length]);
                ps.setInt(7, 1); // Admin
                ps.executeUpdate();
            }

            // Update product stock
            try (PreparedStatement ps = conn.prepareStatement("UPDATE products SET stock = ? WHERE id = ?")) {
                ps.setInt(1, afterStock);
                ps.setInt(2, productId);
                ps.executeUpdate();
            }

            // Add stock movement
            addStockMovement(conn, productId, "ADJUSTMENT", "STOCK_ADJ", null, change, currentStock, afterStock,
                    notes[i % notes.length], 1);
        }
        logger.info("Stock adjustments seeded successfully");
    }

    private static void seedCustomersNew(Connection conn) throws SQLException {
        // Minimal customer data per user request "ga butuh data customers"
        String[][] custs = {
                { "Walk-in Customer", "-", "-", "-" }
        };

        try (PreparedStatement ps = conn
                .prepareStatement(
                        "INSERT OR IGNORE INTO customers (name, phone, address, email, is_active) VALUES (?, ?, ?, ?, 1)")) {
            for (String[] c : custs) {
                ps.setString(1, c[0]); // Name
                ps.setString(2, c[1]); // Phone
                ps.setString(3, c[2]); // Address
                ps.setString(4, c[3]); // Email
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}


