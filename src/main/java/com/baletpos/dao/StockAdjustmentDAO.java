package com.baletpos.dao;

import com.baletpos.config.DatabaseConfig;
import com.baletpos.config.SqlDialect;
import com.baletpos.model.StockAdjustment;
import com.baletpos.model.StockAdjustmentItem;
import com.baletpos.model.StockMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StockAdjustmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(StockAdjustmentDAO.class);
    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public void createAdjustment(StockAdjustment adj) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            String adjNo = generateAdjNumber(conn);
            adj.setAdjNo(adjNo);

            if (adj.getItems() == null || adj.getItems().isEmpty()) {
                throw new SQLException("Item mutasi tidak ditemukan");
            }

            StockAdjustmentItem item = adj.getItems().get(0);
            adj.setProductId(item.getProductId());
            adj.setQuantityChange(item.getQtyDelta());
            adj.setNotes(item.getNote());

            long adjId = insertHeader(conn, adj, item);
            adj.setId(adjId);

            // Update Stock (Delta can be negative)
            int beforeStock = getProductStock(conn, item.getProductId());
            updateProductStock(conn, item.getProductId(), item.getQtyDelta());

            // Insert Movement
            StockMovement mv = new StockMovement();
            mv.setProductId(item.getProductId());
            mv.setMovementType(StockMovement.MovementType.ADJUSTMENT);
            mv.setReferenceType("STOCK_ADJUSTMENT");
            mv.setReferenceId(adjId);
            mv.setQuantityChange(item.getQtyDelta());

            mv.setStockBefore(beforeStock);
            mv.setStockAfter(beforeStock + item.getQtyDelta());

            mv.setCreatedBy(adj.getUserId());
            mv.setNotes("Adj " + adjNo + ": " + item.getNote());

            insertMovement(conn, mv);

            conn.commit();
            logger.info("Stock Adjustment created: {}", adjNo);

        } catch (Exception e) {
            if (conn != null)
                conn.rollback();
            throw new SQLException("Failed to create adjustment", e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private String generateAdjNumber(Connection conn) throws SQLException {
        String datePart = LocalDate.now().format(NO_FMT);
        String prefix = "ADJ-" + datePart + "-";

        String sql = "SELECT COUNT(*) FROM stock_adjustments WHERE adjustment_number LIKE ?";
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

    private long insertHeader(Connection conn, StockAdjustment adj, StockAdjustmentItem item) throws SQLException {
        String sql = "INSERT INTO stock_adjustments (adjustment_number, product_id, adjustment_date, quantity_change, reason, notes, created_by) " +
                "VALUES (?, ?, " + SqlDialect.nowExpression() + ", ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adj.getAdjNo());
            pstmt.setLong(2, item.getProductId());
            pstmt.setInt(3, item.getQtyDelta());
            pstmt.setString(4, adj.getReason());
            pstmt.setString(5, item.getNote());
            pstmt.setLong(6, adj.getUserId());
            pstmt.executeUpdate();

            return fetchIdByNo(conn, adj.getAdjNo());
        }
    }

    private long fetchIdByNo(Connection conn, String no) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM stock_adjustments WHERE adjustment_number = ?")) {
            pstmt.setString(1, no);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getLong(1);
                throw new SQLException("ID lookup failed");
            }
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
     * Fetch all stock adjustments with user info
     */
    public java.util.List<StockAdjustment> findAll() {
        java.util.List<StockAdjustment> list = new java.util.ArrayList<>();
        String sql = """
                SELECT sa.id, sa.adjustment_number, sa.product_id, sa.adjustment_date, sa.quantity_change,
                       sa.reason, sa.notes, u.full_name as user_name, p.sku as product_sku, p.name as product_name
                FROM stock_adjustments sa
                LEFT JOIN users u ON sa.created_by = u.id
                LEFT JOIN products p ON sa.product_id = p.id
                ORDER BY sa.adjustment_date DESC
                """;
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                StockAdjustment adj = new StockAdjustment();
                adj.setId(rs.getLong("id"));
                adj.setAdjNo(rs.getString("adjustment_number"));
                adj.setProductId(rs.getLong("product_id"));
                adj.setQuantityChange(rs.getInt("quantity_change"));
                adj.setReason(rs.getString("reason"));
                adj.setNotes(rs.getString("notes"));
                String createdAt = rs.getString("adjustment_date");
                if (createdAt != null) {
                    adj.setCreatedAt(java.time.LocalDateTime.parse(createdAt.replace(" ", "T")));
                }
                adj.setCreatedByName(rs.getString("user_name"));
                adj.setProductSku(rs.getString("product_sku"));
                adj.setProductName(rs.getString("product_name"));
                list.add(adj);
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch stock adjustments", e);
        }
        return list;
    }

    /**
     * Fetch stock adjustment by ID with items
     */
    public StockAdjustment findById(Long id) {
        String sql = """
                SELECT sa.id, sa.adjustment_number, sa.product_id, sa.adjustment_date, sa.quantity_change,
                       sa.reason, sa.notes, u.full_name as user_name, p.sku as product_sku, p.name as product_name
                FROM stock_adjustments sa
                LEFT JOIN users u ON sa.created_by = u.id
                LEFT JOIN products p ON sa.product_id = p.id
                WHERE sa.id = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    StockAdjustment adj = new StockAdjustment();
                    adj.setId(rs.getLong("id"));
                    adj.setAdjNo(rs.getString("adjustment_number"));
                    adj.setProductId(rs.getLong("product_id"));
                    adj.setQuantityChange(rs.getInt("quantity_change"));
                    adj.setReason(rs.getString("reason"));
                    adj.setNotes(rs.getString("notes"));
                    String createdAt = rs.getString("adjustment_date");
                    if (createdAt != null) {
                        adj.setCreatedAt(java.time.LocalDateTime.parse(createdAt.replace(" ", "T")));
                    }
                    adj.setCreatedByName(rs.getString("user_name"));
                    adj.setProductSku(rs.getString("product_sku"));
                    adj.setProductName(rs.getString("product_name"));
                    adj.setItems(findItemsByAdjustmentId(adj.getId()));
                    return adj;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch stock adjustment by id", e);
        }
        return null;
    }

    /**
     * Fetch items for a specific adjustment
     */
    public java.util.List<StockAdjustmentItem> findItemsByAdjustmentId(Long adjustmentId) {
        java.util.List<StockAdjustmentItem> items = new java.util.ArrayList<>();
        String sql = """
                SELECT sa.id, sa.product_id, sa.quantity_change, sa.notes, p.sku, p.name
                FROM stock_adjustments sa
                LEFT JOIN products p ON sa.product_id = p.id
                WHERE sa.id = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, adjustmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StockAdjustmentItem item = new StockAdjustmentItem();
                    item.setId(rs.getLong("id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQtyDelta(rs.getInt("quantity_change"));
                    item.setNote(rs.getString("notes"));
                    item.setProductSku(rs.getString("sku"));
                    item.setProductName(rs.getString("name"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch adjustment items", e);
        }
        return items;
    }
}


