package com.zenithbazaar.repository;

import com.zenithbazaar.config.DatabaseConfig;
import com.zenithbazaar.model.Order;
import com.zenithbazaar.model.OrderItem;
import com.zenithbazaar.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDao {
    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    public Long createPurchaseTransactional(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO purchases (order_number, customer_id, total_amount, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getOrderNumber());
            ps.setLong(2, order.getCustomerId());
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.setString(4, order.getStatus().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated order ID");
    }

    public void createPurchaseItemTransactional(Connection conn, OrderItem item) throws SQLException {
        String sql = "INSERT INTO purchase_items (purchase_id, product_id, vendor_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, item.getPurchaseId());
            ps.setLong(2, item.getProductId());
            ps.setLong(3, item.getVendorId());
            ps.setInt(4, item.getQuantity());
            ps.setBigDecimal(5, item.getUnitPrice());
            ps.setBigDecimal(6, item.getSubtotal());
            ps.executeUpdate();
        }
    }

    public Optional<Order> findById(Long id) {
        String sql = "SELECT p.id, p.order_number, p.customer_id, u.full_name as customer_name, u.email as customer_email, p.total_amount, p.status, p.created_at, p.updated_at " +
                "FROM purchases p JOIN users u ON p.customer_id = u.id WHERE p.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findItemsByPurchaseId(conn, order.getId()));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding order by id: {}", id, e);
        }
        return Optional.empty();
    }

    public List<Order> findByCustomer(Long customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT p.id, p.order_number, p.customer_id, u.full_name as customer_name, u.email as customer_email, p.total_amount, p.status, p.created_at, p.updated_at " +
                "FROM purchases p JOIN users u ON p.customer_id = u.id WHERE p.customer_id = ? ORDER BY p.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findItemsByPurchaseId(conn, order.getId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding customer orders: {}", customerId, e);
        }
        return orders;
    }

    public List<Order> findByVendor(Long vendorId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT DISTINCT p.id, p.order_number, p.customer_id, u.full_name as customer_name, u.email as customer_email, p.total_amount, p.status, p.created_at, p.updated_at " +
                "FROM purchases p " +
                "JOIN users u ON p.customer_id = u.id " +
                "JOIN purchase_items pi ON p.id = pi.purchase_id " +
                "WHERE pi.vendor_id = ? ORDER BY p.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vendorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    // Filter order items to only those belonging to vendor
                    order.setItems(findItemsByPurchaseIdAndVendor(conn, order.getId(), vendorId));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding vendor orders: {}", vendorId, e);
        }
        return orders;
    }

    public List<Order> findAllAdmin() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT p.id, p.order_number, p.customer_id, u.full_name as customer_name, u.email as customer_email, p.total_amount, p.status, p.created_at, p.updated_at " +
                "FROM purchases p JOIN users u ON p.customer_id = u.id ORDER BY p.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findItemsByPurchaseId(conn, order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            logger.error("Error finding all admin orders", e);
        }
        return orders;
    }

    public boolean updateStatus(Long orderId, OrderStatus status) {
        String sql = "UPDATE purchases SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating order status: {}", orderId, e);
            return false;
        }
    }

    public boolean hasCustomerPurchasedProduct(Long customerId, Long productId) {
        String sql = "SELECT COUNT(*) FROM purchases p " +
                "JOIN purchase_items pi ON p.id = pi.purchase_id " +
                "WHERE p.customer_id = ? AND pi.product_id = ? AND p.status = 'DELIVERED'";
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
            logger.error("Error checking purchased product eligibility", e);
        }
        return false;
    }

    private List<OrderItem> findItemsByPurchaseId(Connection conn, Long purchaseId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT pi.id, pi.purchase_id, pi.product_id, pr.name as product_name, pr.category as product_category, " +
                "pi.vendor_id, vu.full_name as vendor_name, pi.quantity, pi.unit_price, pi.subtotal " +
                "FROM purchase_items pi " +
                "JOIN products pr ON pi.product_id = pr.id " +
                "JOIN users vu ON pi.vendor_id = vu.id " +
                "WHERE pi.purchase_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, purchaseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToOrderItem(rs));
                }
            }
        }
        return items;
    }

    private List<OrderItem> findItemsByPurchaseIdAndVendor(Connection conn, Long purchaseId, Long vendorId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT pi.id, pi.purchase_id, pi.product_id, pr.name as product_name, pr.category as product_category, " +
                "pi.vendor_id, vu.full_name as vendor_name, pi.quantity, pi.unit_price, pi.subtotal " +
                "FROM purchase_items pi " +
                "JOIN products pr ON pi.product_id = pr.id " +
                "JOIN users vu ON pi.vendor_id = vu.id " +
                "WHERE pi.purchase_id = ? AND pi.vendor_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, purchaseId);
            ps.setLong(2, vendorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToOrderItem(rs));
                }
            }
        }
        return items;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setCustomerId(rs.getLong("customer_id"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setCustomerEmail(rs.getString("customer_email"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setStatus(OrderStatus.valueOf(rs.getString("status")));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        o.setUpdatedAt(rs.getTimestamp("updated_at"));
        return o;
    }

    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getLong("id"));
        item.setPurchaseId(rs.getLong("purchase_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setProductCategory(rs.getString("product_category"));
        item.setVendorId(rs.getLong("vendor_id"));
        item.setVendorName(rs.getString("vendor_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setSubtotal(rs.getBigDecimal("subtotal"));
        return item;
    }
}
