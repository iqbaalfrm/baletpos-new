package com.baletpos.util;

import com.baletpos.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserReset {

    public static void main(String[] args) {
        String username = "admin";
        String password = "admin123";

        System.out.println("Resetting password for user: " + username);

        try {
            // Force init to ensure DB exists
            DatabaseConfig.initialize();

            String hash = PasswordUtil.hashPassword(password);
            System.out.println("Generated Hash: " + hash);

            try (Connection conn = DatabaseConfig.getConnection()) {
                // Try update first
                String updateSql = "UPDATE users SET password_hash = ?, is_active = 1 WHERE username = ?";
                try (PreparedStatement adminStmt = conn.prepareStatement(updateSql)) {
                    adminStmt.setString(1, hash);
                    adminStmt.setString(2, username);
                    int updated = adminStmt.executeUpdate();

                    if (updated > 0) {
                        System.out.println("SUCCESS: User 'admin' updated.");
                    } else {
                        // Insert if not exists
                        System.out.println("User not found, inserting new admin...");
                        String insertSql = "INSERT INTO users (username, password_hash, full_name, role, is_active) VALUES (?, ?, 'Administrator', 'ADMIN', 1)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, username);
                            insertStmt.setString(2, hash);
                            insertStmt.executeUpdate();
                            System.out.println("SUCCESS: User 'admin' inserted.");
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Force exit because JavaFX threads might linger from DatabaseConfig init if it
        // touched anything related
        System.exit(0);
    }
}


