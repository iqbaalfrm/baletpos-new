package com.baletpos.dao;

import com.baletpos.config.DatabaseConfig;
import com.baletpos.config.SqlDialect;
import com.baletpos.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk Audit Log
 */
public class AuditLogDAO {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogDAO.class);
    private static final DateTimeFormatter DB_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void insert(AuditLog auditLog) {
        String sql = "INSERT INTO audit_logs (user_id, action, table_name, record_id, old_values, new_values, ip_address) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, auditLog.getUserId());
            pstmt.setString(2, auditLog.getAction());
            pstmt.setString(3, auditLog.getTableName());
            pstmt.setObject(4, auditLog.getRecordId());
            pstmt.setString(5, auditLog.getOldValues());
            pstmt.setString(6, auditLog.getNewValues());
            pstmt.setString(7, auditLog.getIpAddress());

            pstmt.executeUpdate();

            // SQLite style: get last inserted ID
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT " + SqlDialect.lastInsertIdExpression())) {
                if (rs.next()) {
                    auditLog.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting audit log", e);
        }
    }

    /**
     * Helper method untuk logging aksi penting
     */
    public void log(Long userId, String action, String tableName, Long recordId, String oldValues, String newValues) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setTableName(tableName);
        auditLog.setRecordId(recordId);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        insert(auditLog);
    }

    public List<AuditLog> findAll() {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.username FROM audit_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "ORDER BY al.created_at DESC LIMIT 500";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all audit logs", e);
        }
        return logs;
    }

    public List<AuditLog> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.username FROM audit_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "WHERE " + SqlDialect.dateExpression("al.created_at") + " BETWEEN ? AND ? " +
                "ORDER BY al.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding audit logs by date range", e);
        }
        return logs;
    }

    public List<AuditLog> findByAction(String action) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.username FROM audit_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "WHERE al.action = ? " +
                "ORDER BY al.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, action);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding audit logs by action: {}", action, e);
        }
        return logs;
    }

    public List<AuditLog> findByUserId(Long userId) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.username FROM audit_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "WHERE al.user_id = ? " +
                "ORDER BY al.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding audit logs by user id: {}", userId, e);
        }
        return logs;
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("id"));
        log.setUserId(rs.getLong("user_id"));
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        log.setTableName(rs.getString("table_name"));

        Object recordIdObj = rs.getObject("record_id");
        if (recordIdObj != null) {
            log.setRecordId(((Number) recordIdObj).longValue());
        }

        log.setOldValues(rs.getString("old_values"));
        log.setNewValues(rs.getString("new_values"));
        log.setIpAddress(rs.getString("ip_address"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            log.setCreatedAt(LocalDateTime.parse(createdAtStr, DB_DATE_FMT));
        }

        return log;
    }
}


