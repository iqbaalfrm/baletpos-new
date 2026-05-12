package com.baletpos.util;

import com.baletpos.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SchemaInspector {
    public static void inspect() {
        String[] tables = {"sales", "sale_items", "products", "stock_movements", "users", "customers"};
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("=== INTERNAL SCHEMA INSPECTION ===");
            for (String table : tables) {
                System.out.println("TABLE: " + table);
                try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
                    while (rs.next()) {
                        System.out.printf("  - %s (%s)%n", rs.getString("name"), rs.getString("type"));
                    }
                }
            }
            System.out.println("==================================");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        inspect();
    }
}


