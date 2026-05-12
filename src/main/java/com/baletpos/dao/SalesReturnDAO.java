package com.baletpos.dao;

import com.baletpos.config.DatabaseConfig;
import com.baletpos.config.SqlDialect;
import com.baletpos.model.SalesReturn;
import com.baletpos.model.SalesReturnItem;
import com.baletpos.model.StockMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SalesReturnDAO {
    private static final Logger logger = LoggerFactory.getLogger(SalesReturnDAO.class);
    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public void createReturn(SalesReturn salesReturn) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Generate Return Number (RT-YYYYMMDD-XXXX)
            String returnNo = generateReturnNumber(conn);
            salesReturn.setReturnNo(returnNo);

            // 2. Insert Header
            long returnId = insertHeader(conn, salesReturn);
            salesReturn.setId(returnId);

            // 3. Process Items
            for (SalesReturnItem item : salesReturn.getItems()) {
                item.setSalesReturnId(returnId);

                // Insert Item
                insertItem(conn, item);

                // Update Stock (ADD)
                updateProductStock(conn, item.getProductId(), item.getQtyReturn());

                // Insert Movement
                StockMovement mv = new StockMovement();
                mv.setProductId(item.getProductId());
                mv.setMovementType(StockMovement.MovementType.SALE_RETURN);
                mv.setReferenceType("SALES_RETURN");
                mv.setReferenceId(returnId);
                mv.setQuantityChange(item.getQtyReturn()); // Positive

                // Need current stock for consistency (optional but good practice)
                int currentStock = getProductStock(conn, item.getProductId());
                mv.setStockAfter(currentStock);
                mv.setStockBefore(currentStock - item.getQtyReturn());

                mv.setCreatedBy(salesReturn.getUserId());
                mv.setNotes("Return " + returnNo);

                insertMovement(conn, mv);
            }

            conn.commit();
            logger.info("Sales Return created successfully: {}", returnNo);

        } catch (Exception e) {
            if (conn != null)
                conn.rollback();
            throw new SQLException("Failed to create sales return", e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private String generateReturnNumber(Connection conn) throws SQLException {
        String datePart = LocalDate.now().format(NO_FMT);
        String prefix = "RT-" + datePart + "-";

        // Simple count logic for brevity (Sequence table better but this is acceptable
        // for now)
        String sql = "SELECT COUNT(*) FROM sales_returns WHERE return_number LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                if (rs.next())
                    count = rs.getInt(1);
                return prefix + String.format("%04d", count + 1);
            }
        }
    }

    private long insertHeader(Connection conn, SalesReturn ret) throws SQLException {
        String sql = "INSERT INTO sales_returns (return_number, sale_id, return_date, total_amount, reason, status, created_by) " +
                "VALUES (?, ?, " + SqlDialect.nowExpression() + ", ?, ?, 'COMPLETED', ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ret.getReturnNo());
            pstmt.setLong(2, ret.getSaleId());
            pstmt.setBigDecimal(3, ret.getTotalAmount());
            pstmt.setString(4, ret.getNotes() != null ? ret.getNotes() : "Retur Penjualan");
            pstmt.setLong(5, ret.getUserId());
            pstmt.executeUpdate();

            return fetchIdByNo(conn, ret.getReturnNo());
        }
    }

    private long fetchIdByNo(Connection conn, String no) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM sales_returns WHERE return_number = ?")) {
            pstmt.setString(1, no);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getLong(1);
                throw new SQLException("ID lookup failed");
            }
        }
    }

    private void insertItem(Connection conn, SalesReturnItem item) throws SQLException {
        String sql = "INSERT INTO sales_return_items (sales_return_id, sale_item_id, product_id, quantity, unit_price, hpp_per_unit, subtotal) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, item.getSalesReturnId());
            pstmt.setLong(2, item.getSaleItemId());
            pstmt.setLong(3, item.getProductId());
            pstmt.setInt(4, item.getQtyReturn());
            pstmt.setBigDecimal(5, item.getUnitPrice());
            pstmt.setBigDecimal(6, item.getSnapshotHpp());
            pstmt.setBigDecimal(7, item.getLineTotal());
            pstmt.executeUpdate();
        }
    }

    private void updateProductStock(Connection conn, Long productId, int change) throws SQLException {
        String sql = "UPDATE products SET stock = stock + ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, change);
            pstmt.setLong(2, productId);
            pstmt.executeUpdate();
        }
    }

    private void insertMovement(Connection conn, StockMovement mv) throws SQLException {
        String sql = "INSERT INTO stock_movements (product_id, movement_type, reference_type, reference_id, quantity_change, stock_before, stock_after, notes, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, " + SqlDialect.nowExpression() + ")";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, mv.getProductId());
            pstmt.setString(2, mv.getMovementType().name());
            pstmt.setString(3, mv.getReferenceType());
            pstmt.setLong(4, mv.getReferenceId());
            pstmt.setInt(5, mv.getQuantityChange());
            pstmt.setInt(6, mv.getStockBefore());
            pstmt.setInt(7, mv.getStockAfter());
            pstmt.setString(8, mv.getNotes());
            pstmt.setLong(9, mv.getCreatedBy());
            pstmt.executeUpdate();
        }
    }

    private int getProductStock(Connection conn, Long productId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT stock FROM products WHERE id = ?")) {
            pstmt.setLong(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get total quantity returned for a specific product in a specific sale
     */
    public int getReturnedQuantity(Long saleId, Long productId) {
        String sql = """
                SELECT SUM(sri.quantity)
                FROM sales_return_items sri
                JOIN sales_returns sr ON sri.sales_return_id = sr.id
                WHERE sr.sale_id = ? AND sri.product_id = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, saleId);
            pstmt.setLong(2, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch returned quantity", e);
        }
        return 0;
    }

    /**
     * Fetch all sales returns with sale invoice info
     */
    public java.util.List<SalesReturn> findAll() {
        java.util.List<SalesReturn> list = new java.util.ArrayList<>();
        String sql = """
                SELECT sr.id, sr.return_number, sr.sale_id, sr.created_at, sr.reason, sr.total_amount,
                       s.invoice_number, u.full_name as user_name
                FROM sales_returns sr
                LEFT JOIN sales s ON sr.sale_id = s.id
                LEFT JOIN users u ON sr.created_by = u.id
                ORDER BY sr.created_at DESC
                """;
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                SalesReturn ret = new SalesReturn();
                ret.setId(rs.getLong("id"));
                ret.setReturnNo(rs.getString("return_number"));
                ret.setSaleId(rs.getLong("sale_id"));
                String createdAt = rs.getString("created_at");
                if (createdAt != null) {
                    ret.setCreatedAt(java.time.LocalDateTime.parse(createdAt.replace(" ", "T")));
                }
                ret.setNotes(rs.getString("reason"));
                ret.setTotalAmount(rs.getBigDecimal("total_amount"));
                ret.setSaleInvoiceNumber(rs.getString("invoice_number"));
                ret.setCreatedByName(rs.getString("user_name"));
                list.add(ret);
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch sales returns", e);
        }
        return list;
    }

    /**
     * Fetch sales return by ID with items
     */
    public SalesReturn findById(Long id) {
        String sql = """
                SELECT sr.id, sr.return_number, sr.sale_id, sr.created_at, sr.reason, sr.total_amount,
                       s.invoice_number, u.full_name as user_name
                FROM sales_returns sr
                LEFT JOIN sales s ON sr.sale_id = s.id
                LEFT JOIN users u ON sr.created_by = u.id
                WHERE sr.id = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    SalesReturn ret = new SalesReturn();
                    ret.setId(rs.getLong("id"));
                    ret.setReturnNo(rs.getString("return_number"));
                    ret.setSaleId(rs.getLong("sale_id"));
                    String createdAt = rs.getString("created_at");
                    if (createdAt != null) {
                        ret.setCreatedAt(java.time.LocalDateTime.parse(createdAt.replace(" ", "T")));
                    }
                    ret.setNotes(rs.getString("reason"));
                    ret.setTotalAmount(rs.getBigDecimal("total_amount"));
                    ret.setSaleInvoiceNumber(rs.getString("invoice_number"));
                    ret.setCreatedByName(rs.getString("user_name"));
                    ret.setItems(findItemsByReturnId(ret.getId()));
                    return ret;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch sales return by id", e);
        }
        return null;
    }

    /**
     * Fetch items for a specific return
     */
    public java.util.List<SalesReturnItem> findItemsByReturnId(Long returnId) {
        java.util.List<SalesReturnItem> items = new java.util.ArrayList<>();
        String sql = """
                SELECT sri.id, sri.sales_return_id, sri.sale_item_id, sri.product_id, sri.quantity, sri.unit_price,
                       sri.hpp_per_unit, sri.subtotal, p.sku, p.name
                FROM sales_return_items sri
                LEFT JOIN products p ON sri.product_id = p.id
                WHERE sri.sales_return_id = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, returnId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SalesReturnItem item = new SalesReturnItem();
                    item.setId(rs.getLong("id"));
                    item.setSalesReturnId(rs.getLong("sales_return_id"));
                    item.setSaleItemId(rs.getLong("sale_item_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQtyReturn(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSnapshotHpp(rs.getBigDecimal("hpp_per_unit"));
                    item.setLineTotal(rs.getBigDecimal("subtotal"));
                    item.setProductSku(rs.getString("sku"));
                    item.setProductName(rs.getString("name"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch return items", e);
        }
        return items;
    }
}


