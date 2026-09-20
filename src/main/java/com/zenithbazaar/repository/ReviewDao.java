package com.zenithbazaar.repository;

import com.zenithbazaar.config.DatabaseConfig;
import com.zenithbazaar.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {
    private static final Logger logger = LoggerFactory.getLogger(ReviewDao.class);

    public Review save(Review review) {
        String sql = "INSERT INTO product_reviews (product_id, customer_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getProductId());
            ps.setLong(2, review.getCustomerId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        review.setId(rs.getLong(1));
                    }
                }
            }
            return review;
        } catch (SQLException e) {
            logger.error("Error saving review", e);
            throw new RuntimeException("Database error saving review", e);
        }
    }

    public List<Review> findByProductId(Long productId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.id, r.product_id, p.name as product_name, r.customer_id, u.full_name as customer_name, r.rating, r.comment, r.created_at " +
                "FROM product_reviews r " +
                "JOIN products p ON r.product_id = p.id " +
                "JOIN users u ON r.customer_id = u.id " +
                "WHERE r.product_id = ? ORDER BY r.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setProductId(rs.getLong("product_id"));
                    r.setProductName(rs.getString("product_name"));
                    r.setCustomerId(rs.getLong("customer_id"));
                    r.setCustomerName(rs.getString("customer_name"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    reviews.add(r);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching reviews for productId: {}", productId, e);
        }
        return reviews;
    }

    public boolean hasUserReviewedProduct(Long customerId, Long productId) {
        String sql = "SELECT COUNT(*) FROM product_reviews WHERE customer_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking user review status", e);
        }
        return false;
    }
}
