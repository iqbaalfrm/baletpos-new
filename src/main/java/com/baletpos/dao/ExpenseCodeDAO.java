package com.baletpos.dao;

import com.baletpos.config.DatabaseConfig;
import com.baletpos.config.SqlDialect;
import com.baletpos.model.ExpenseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object untuk Kode Biaya
 */
public class ExpenseCodeDAO {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseCodeDAO.class);
    private static final DateTimeFormatter DB_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<ExpenseCode> findAll() {
        List<ExpenseCode> codes = new ArrayList<>();
        String sql = "SELECT * FROM expense_codes WHERE is_active = 1 ORDER BY code";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                codes.add(mapResultSetToExpenseCode(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all expense codes", e);
        }
        return codes;
    }

    public Optional<ExpenseCode> findById(Long id) {
        String sql = "SELECT * FROM expense_codes WHERE id = ? AND is_active = 1";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToExpenseCode(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding expense code by id: {}", id, e);
        }
        return Optional.empty();
    }

    public Optional<ExpenseCode> findByCode(String code) {
        String sql = "SELECT * FROM expense_codes WHERE code = ? AND is_active = 1";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToExpenseCode(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding expense code by code: {}", code, e);
        }
        return Optional.empty();
    }

    public void save(ExpenseCode expenseCode) {
        if (expenseCode.getId() == null) {
            insert(expenseCode);
        } else {
            update(expenseCode);
        }
    }

    private void insert(ExpenseCode expenseCode) {
        String sql = "INSERT INTO expense_codes (code, name, description) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expenseCode.getCode());
            pstmt.setString(2, expenseCode.getName());
            pstmt.setString(3, expenseCode.getDescription());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // SQLite style: get last inserted ID
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT " + SqlDialect.lastInsertIdExpression())) {
                    if (rs.next()) {
                        expenseCode.setId(rs.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting expense code", e);
            throw new RuntimeException("Gagal menyimpan kode biaya");
        }
    }

    private void update(ExpenseCode expenseCode) {
        String sql = "UPDATE expense_codes SET name = ?, description = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expenseCode.getName());
            pstmt.setString(2, expenseCode.getDescription());
            pstmt.setLong(3, expenseCode.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating expense code", e);
            throw new RuntimeException("Error updating expense code: " + e.getMessage());
        }
    }

    public void delete(Long id) {
        String sql = "UPDATE expense_codes SET is_active = 0 WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting expense code", e);
            throw new RuntimeException("Error deleting expense code: " + e.getMessage());
        }
    }

    private ExpenseCode mapResultSetToExpenseCode(ResultSet rs) throws SQLException {
        ExpenseCode ec = new ExpenseCode();
        ec.setId(rs.getLong("id"));
        ec.setCode(rs.getString("code"));
        ec.setName(rs.getString("name"));
        ec.setDescription(rs.getString("description"));
        ec.setActive(rs.getInt("is_active") == 1);

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            ec.setCreatedAt(LocalDateTime.parse(createdAtStr, DB_DATE_FMT));
        }

        return ec;
    }
}


