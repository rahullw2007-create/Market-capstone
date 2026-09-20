package com.zenithbazaar.repository;

import com.zenithbazaar.config.DatabaseConfig;
import com.zenithbazaar.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDao {
    private static final Logger logger = LoggerFactory.getLogger(ProductDao.class);

    public Optional<Product> findById(Long id) {
        String sql = "SELECT p.id, p.vendor_id, u.full_name as vendor_name, p.name, p.description, p.price, p.quantity, p.category, p.image_url, p.is_active, p.created_at, p.updated_at " +
                "FROM products p JOIN users u ON p.vendor_id = u.id WHERE p.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding product by id: {}", id, e);
        }
        return Optional.empty();
    }

    public List<Product> findCatalog(String search, String category, String sort, int offset, int limit) {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.vendor_id, u.full_name as vendor_name, p.name, p.description, p.price, p.quantity, p.category, p.image_url, p.is_active, p.created_at, p.updated_at, " +
                "COALESCE(AVG(r.rating), 0.0) as avg_rating, COUNT(r.id) as review_count " +
                "FROM products p " +
                "JOIN users u ON p.vendor_id = u.id " +
                "LEFT JOIN product_reviews r ON p.id = r.product_id " +
                "WHERE p.is_active = true AND u.is_active = true "
        );

        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String term = "%" + search.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            sql.append("AND p.category = ? ");
            params.add(category.trim());
        }

        sql.append("GROUP BY p.id, p.vendor_id, u.full_name, p.name, p.description, p.price, p.quantity, p.category, p.image_url, p.is_active, p.created_at, p.updated_at ");

        if ("price_asc".equalsIgnoreCase(sort)) {
            sql.append("ORDER BY p.price ASC ");
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            sql.append("ORDER BY p.price DESC ");
        } else if ("rating".equalsIgnoreCase(sort)) {
            sql.append("ORDER BY avg_rating DESC ");
        } else {
            sql.append("ORDER BY p.id DESC ");
        }

        sql.append("LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = mapResultSetToProduct(rs);
                    p.setAverageRating(rs.getDouble("avg_rating"));
                    p.setReviewCount(rs.getInt("review_count"));
                    products.add(p);
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching catalog products", e);
        }
        return products;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM products WHERE is_active = true ORDER BY category ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            logger.error("Error fetching categories", e);
        }
        return categories;
    }

    public List<Product> findByVendorId(Long vendorId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.id, p.vendor_id, u.full_name as vendor_name, p.name, p.description, p.price, p.quantity, p.category, p.image_url, p.is_active, p.created_at, p.updated_at " +
                "FROM products p JOIN users u ON p.vendor_id = u.id WHERE p.vendor_id = ? ORDER BY p.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vendorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching vendor products for vendorId: {}", vendorId, e);
        }
        return products;
    }

    public List<Product> findAllAdmin() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.id, p.vendor_id, u.full_name as vendor_name, p.name, p.description, p.price, p.quantity, p.category, p.image_url, p.is_active, p.created_at, p.updated_at " +
                "FROM products p JOIN users u ON p.vendor_id = u.id ORDER BY p.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all products for admin", e);
        }
        return products;
    }

    public Product save(Product product) {
        String sql = "INSERT INTO products (vendor_id, name, description, price, quantity, category, image_url, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, product.getVendorId());
            ps.setString(2, product.getName().trim());
            ps.setString(3, product.getDescription());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getQuantity());
            ps.setString(6, product.getCategory().trim());
            ps.setString(7, product.getImageUrl());
            ps.setBoolean(8, product.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        product.setId(rs.getLong(1));
                    }
                }
            }
            return product;
        } catch (SQLException e) {
            logger.error("Error saving product", e);
            throw new RuntimeException("Database error saving product", e);
        }
    }

    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, quantity = ?, category = ?, image_url = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND vendor_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName().trim());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getQuantity());
            ps.setString(5, product.getCategory().trim());
            ps.setString(6, product.getImageUrl());
            ps.setBoolean(7, product.isActive());
            ps.setLong(8, product.getId());
            ps.setLong(9, product.getVendorId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating product: {}", product.getId(), e);
            return false;
        }
    }

    public boolean updateStatus(Long id, boolean active) {
        String sql = "UPDATE products SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating product status for id: {}", id, e);
            return false;
        }
    }

    public boolean reduceStockTransactional(Connection conn, Long productId, int quantityToReduce) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity - ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityToReduce);
            ps.setLong(2, productId);
            ps.setInt(3, quantityToReduce);
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setVendorId(rs.getLong("vendor_id"));
        p.setVendorName(rs.getString("vendor_name"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        p.setActive(rs.getBoolean("is_active"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
}
