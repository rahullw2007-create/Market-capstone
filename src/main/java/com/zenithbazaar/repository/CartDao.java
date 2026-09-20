package com.zenithbazaar.repository;

import com.zenithbazaar.config.DatabaseConfig;
import com.zenithbazaar.model.CartItem;
import com.zenithbazaar.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartDao {
    private static final Logger logger = LoggerFactory.getLogger(CartDao.class);

    public List<CartItem> findByCustomerId(Long customerId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT c.id, c.customer_id, c.product_id, c.quantity, c.created_at, c.updated_at, " +
                "p.name, p.description, p.price, p.quantity as stock, p.category, p.image_url, p.is_active, p.vendor_id, u.full_name as vendor_name " +
                "FROM shopping_cart c " +
                "JOIN products p ON c.product_id = p.id " +
                "JOIN users u ON p.vendor_id = u.id " +
                "WHERE c.customer_id = ? ORDER BY c.id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setCustomerId(rs.getLong("customer_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    item.setUpdatedAt(rs.getTimestamp("updated_at"));

                    Product p = new Product();
                    p.setId(rs.getLong("product_id"));
                    p.setName(rs.getString("name"));
                    p.setDescription(rs.getString("description"));
                    p.setPrice(rs.getBigDecimal("price"));
                    p.setQuantity(rs.getInt("stock"));
                    p.setCategory(rs.getString("category"));
                    p.setImageUrl(rs.getString("image_url"));
                    p.setActive(rs.getBoolean("is_active"));
                    p.setVendorId(rs.getLong("vendor_id"));
                    p.setVendorName(rs.getString("vendor_name"));

                    item.setProduct(p);
                    item.setSubtotal(p.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching cart for customerId: {}", customerId, e);
        }
        return items;
    }

    public Optional<CartItem> findByCustomerAndProduct(Long customerId, Long productId) {
        String sql = "SELECT id, customer_id, product_id, quantity, created_at, updated_at FROM shopping_cart WHERE customer_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setCustomerId(rs.getLong("customer_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    item.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return Optional.of(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking cart item", e);
        }
        return Optional.empty();
    }

    public boolean addOrUpdateItem(Long customerId, Long productId, int quantity) {
        Optional<CartItem> existing = findByCustomerAndProduct(customerId, productId);
        if (existing.isPresent()) {
            int newQty = existing.get().getQuantity() + quantity;
            return updateQuantity(customerId, productId, newQty);
        } else {
            String sql = "INSERT INTO shopping_cart (customer_id, product_id, quantity) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, customerId);
                ps.setLong(2, productId);
                ps.setInt(3, quantity);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                logger.error("Error inserting cart item", e);
                return false;
            }
        }
    }

    public boolean updateQuantity(Long customerId, Long productId, int quantity) {
        if (quantity <= 0) {
            return removeItem(customerId, productId);
        }
        String sql = "UPDATE shopping_cart SET quantity = ?, updated_at = CURRENT_TIMESTAMP WHERE customer_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, customerId);
            ps.setLong(3, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating cart quantity", e);
            return false;
        }
    }

    public boolean removeItem(Long customerId, Long productId) {
        String sql = "DELETE FROM shopping_cart WHERE customer_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error removing cart item", e);
            return false;
        }
    }

    public boolean clearCartTransactional(Connection conn, Long customerId) throws SQLException {
        String sql = "DELETE FROM shopping_cart WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            return ps.executeUpdate() >= 0;
        }
    }

    public boolean clearCart(Long customerId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return clearCartTransactional(conn, customerId);
        } catch (SQLException e) {
            logger.error("Error clearing cart for customerId: {}", customerId, e);
            return false;
        }
    }
}
