package com.baletpos.dao;

import com.baletpos.config.DatabaseConfig;
import com.baletpos.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO {
    private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE is_active = 1";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all customers", e);
        }
        return customers;
    }

    public List<Customer> search(String query) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE is_active = 1 AND (name LIKE ? OR phone LIKE ?)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching customers with query: {}", query, e);
        }
        return customers;
    }

    public Optional<Customer> findById(Long id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding customer by id: {}", id, e);
        }
        return Optional.empty();
    }

    public List<Customer> findAllPaged(int limit, int offset, String query) {
        List<Customer> customers = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM customers WHERE is_active = 1");

        if (query != null && !query.isEmpty()) {
            sql.append(" AND (name LIKE ? OR phone LIKE ? OR email LIKE ?)");
        }
        sql.append(" ORDER BY name ASC LIMIT ? OFFSET ?");

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (query != null && !query.isEmpty()) {
                String pattern = "%" + query + "%";
                pstmt.setString(paramIndex++, pattern);
                pstmt.setString(paramIndex++, pattern);
                pstmt.setString(paramIndex++, pattern);
            }
            pstmt.setInt(paramIndex++, limit);
            pstmt.setInt(paramIndex, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding paged customers", e);
        }
        return customers;
    }

    public int countFiltered(String query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM customers WHERE is_active = 1");

        if (query != null && !query.isEmpty()) {
            sql.append(" AND (name LIKE ? OR phone LIKE ? OR email LIKE ?)");
        }

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            if (query != null && !query.isEmpty()) {
                String pattern = "%" + query + "%";
                pstmt.setString(1, pattern);
                pstmt.setString(2, pattern);
                pstmt.setString(3, pattern);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting customers", e);
        }
        return 0;
    }

    public void save(Customer customer) {
        if (customer.getId() == null) {
            insert(customer);
        } else {
            update(customer);
        }
    }

    public void insert(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, address, email, notes, is_active) VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getAddress());
            pstmt.setString(4, customer.getEmail());
            pstmt.setString(5, customer.getNotes());
            pstmt.executeUpdate();

            logger.info("Customer created: {}", customer.getName());
        } catch (SQLException e) {
            logger.error("Error inserting customer", e);
        }
    }

    public void update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ?, address = ?, email = ?, notes = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getAddress());
            pstmt.setString(4, customer.getEmail());
            pstmt.setString(5, customer.getNotes());
            pstmt.setLong(6, customer.getId());
            pstmt.executeUpdate();

            logger.info("Customer updated: {}", customer.getName());
        } catch (SQLException e) {
            logger.error("Error updating customer", e);
        }
    }

    public void delete(Long id) {
        // Soft delete - set is_active = 0
        String sql = "UPDATE customers SET is_active = 0 WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

            logger.info("Customer deleted (soft): {}", id);
        } catch (SQLException e) {
            logger.error("Error deleting customer", e);
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setEmail(rs.getString("email"));
        c.setNotes(rs.getString("notes"));
        c.setActive(rs.getInt("is_active") == 1);
        return c;
    }
}


