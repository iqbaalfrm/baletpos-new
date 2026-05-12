package com.baletpos.dao;

import com.baletpos.config.DatabaseConfig;
import com.baletpos.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private static final DateTimeFormatter DB_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
        }
        return Optional.empty();
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setActive(rs.getInt("is_active") == 1);
        
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            user.setCreatedAt(LocalDateTime.parse(createdAtStr, DB_DATE_FMT));
        }
        
        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null) {
            user.setUpdatedAt(LocalDateTime.parse(updatedAtStr, DB_DATE_FMT));
        }
        
        return user;
    }
}


