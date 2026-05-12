package com.baletpos.dao;

import com.baletpos.config.DatabaseConfig;
import com.baletpos.config.SqlDialect;
import com.baletpos.model.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object untuk Supplier
 */
public class SupplierDAO {
    private static final Logger logger = LoggerFactory.getLogger(SupplierDAO.class);
    private static final DateTimeFormatter DB_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE is_active = 1 ORDER BY code";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all suppliers", e);
        }
        return suppliers;
    }

    public List<Supplier> findAllPaged(int limit, int offset, String query) {
        List<Supplier> suppliers = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM suppliers WHERE is_active = 1 ");

        if (query != null && !query.isBlank()) {
            sql.append("AND (code LIKE ? OR name LIKE ?) ");
        }

        sql.append("ORDER BY code LIMIT ? OFFSET ?");

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (query != null && !query.isBlank()) {
                String pattern = "%" + query + "%";
                pstmt.setString(idx++, pattern);
                pstmt.setString(idx++, pattern);
            }
            pstmt.setInt(idx++, limit);
            pstmt.setInt(idx, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding paged suppliers", e);
        }
        return suppliers;
    }

    public int countFiltered(String query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM suppliers WHERE is_active = 1 ");
        if (query != null && !query.isBlank()) {
            sql.append("AND (code LIKE ? OR name LIKE ?) ");
        }

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (query != null && !query.isBlank()) {
                String pattern = "%" + query + "%";
                pstmt.setString(idx++, pattern);
                pstmt.setString(idx, pattern);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting suppliers", e);
        }
        return 0;
    }

    public Optional<Supplier> findById(Long id) {
        String sql = "SELECT * FROM suppliers WHERE id = ? AND is_active = 1";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding supplier by id: {}", id, e);
        }
        return Optional.empty();
    }

    public Optional<Supplier> findByCode(String code) {
        String sql = "SELECT * FROM suppliers WHERE code = ? AND is_active = 1";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding supplier by code: {}", code, e);
        }
        return Optional.empty();
    }

    public List<Supplier> search(String query) {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE is_active = 1 AND (code LIKE ? OR name LIKE ?) ORDER BY code";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching suppliers with query: {}", query, e);
        }
        return suppliers;
    }

    public void save(Supplier supplier) {
        if (supplier.getId() == null) {
            insert(supplier);
        } else {
            update(supplier);
        }
    }

    private void insert(Supplier supplier) {
        String sql = "INSERT INTO suppliers (code, name, contact, address, phone, email) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplier.getCode());
            pstmt.setString(2, supplier.getName());
            pstmt.setString(3, supplier.getContact());
            pstmt.setString(4, supplier.getAddress());
            pstmt.setString(5, supplier.getPhone());
            pstmt.setString(6, supplier.getEmail());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // SQLite style: get last inserted ID
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT " + SqlDialect.lastInsertIdExpression())) {
                    if (rs.next()) {
                        supplier.setId(rs.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting supplier", e);
            throw new RuntimeException("Gagal menyimpan supplier");
        }
    }

    private void update(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact = ?, address = ?, phone = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContact());
            pstmt.setString(3, supplier.getAddress());
            pstmt.setString(4, supplier.getPhone());
            pstmt.setString(5, supplier.getEmail());
            pstmt.setLong(6, supplier.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating supplier", e);
            throw new RuntimeException("Error updating supplier: " + e.getMessage());
        }
    }

    public void delete(Long id) {
        String sql = "UPDATE suppliers SET is_active = 0 WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting supplier", e);
            throw new RuntimeException("Error deleting supplier: " + e.getMessage());
        }
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getLong("id"));
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));
        s.setContact(rs.getString("contact"));
        s.setAddress(rs.getString("address"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setActive(rs.getInt("is_active") == 1);

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            s.setCreatedAt(LocalDateTime.parse(createdAtStr, DB_DATE_FMT));
        }

        return s;
    }
}


