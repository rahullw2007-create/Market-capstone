package com.zenithbazaar.service;

import com.zenithbazaar.config.DatabaseConfig;
import com.zenithbazaar.dto.CheckoutRequest;
import com.zenithbazaar.dto.UserDto;
import com.zenithbazaar.exception.ForbiddenException;
import com.zenithbazaar.exception.NotFoundException;
import com.zenithbazaar.exception.ValidationException;
import com.zenithbazaar.model.*;
import com.zenithbazaar.repository.CartDao;
import com.zenithbazaar.repository.OrderDao;
import com.zenithbazaar.repository.ProductDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderDao orderDao;
    private final CartDao cartDao;
    private final ProductDao productDao;

    public OrderService() {
        this(new OrderDao(), new CartDao(), new ProductDao());
    }

    public OrderService(OrderDao orderDao, CartDao cartDao, ProductDao productDao) {
        this.orderDao = orderDao;
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    public Order checkoutTransactional(Long customerId, CheckoutRequest req) {
        List<CartItem> cartItems = cartDao.findByCustomerId(customerId);
        if (cartItems.isEmpty()) {
            throw new ValidationException("Your shopping cart is empty");
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            BigDecimal grandTotal = BigDecimal.ZERO;

            // 1. Verify products & recalculate prices & stock from database in transaction
            for (CartItem item : cartItems) {
                Product freshProduct = productDao.findById(item.getProductId())
                        .orElseThrow(() -> new ValidationException("Product " + item.getProductId() + " no longer exists"));

                if (!freshProduct.isActive()) {
                    throw new ValidationException("Product '" + freshProduct.getName() + "' is no longer available");
                }

                if (item.getQuantity() > freshProduct.getQuantity()) {
                    throw new ValidationException("Insufficient stock for '" + freshProduct.getName() + "'. Available: " + freshProduct.getQuantity() + ", requested: " + item.getQuantity());
                }

                BigDecimal unitPrice = freshProduct.getPrice();
                BigDecimal subtotal = unitPrice.multiply(new BigDecimal(item.getQuantity()));
                grandTotal = grandTotal.add(subtotal);

                item.setProduct(freshProduct);
                item.setSubtotal(subtotal);
            }

            // 2. Create Purchase Order
            Order order = new Order();
            order.setOrderNumber(generateOrderNumber());
            order.setCustomerId(customerId);
            order.setTotalAmount(grandTotal);
            order.setStatus(OrderStatus.NEW);

            Long purchaseId = orderDao.createPurchaseTransactional(conn, order);
            order.setId(purchaseId);

            // 3. Create Purchase Items & Reduce Product Inventory
            for (CartItem item : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setPurchaseId(purchaseId);
                orderItem.setProductId(item.getProductId());
                orderItem.setVendorId(item.getProduct().getVendorId());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setUnitPrice(item.getProduct().getPrice());
                orderItem.setSubtotal(item.getSubtotal());

                orderDao.createPurchaseItemTransactional(conn, orderItem);

                boolean stockReduced = productDao.reduceStockTransactional(conn, item.getProductId(), item.getQuantity());
                if (!stockReduced) {
                    throw new SQLException("Failed to reduce stock for product: " + item.getProduct().getName());
                }
            }

            // 4. Clear Customer Shopping Cart
            cartDao.clearCartTransactional(conn, customerId);

            // 5. Commit Transaction
            conn.commit();
            logger.info("Order {} successfully created for customer {}", order.getOrderNumber(), customerId);

            return orderDao.findById(purchaseId).orElse(order);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Transaction rolled back for customer {}", customerId);
                } catch (SQLException rollbackEx) {
                    logger.error("Error during transaction rollback", rollbackEx);
                }
            }
            if (e instanceof ValidationException) {
                throw (ValidationException) e;
            }
            throw new RuntimeException("Checkout failed due to system error: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.error("Error closing connection after transaction", closeEx);
                }
            }
        }
    }

    public List<Order> getCustomerOrders(Long customerId) {
        return orderDao.findByCustomer(customerId);
    }

    public List<Order> getVendorOrders(Long vendorId) {
        return orderDao.findByVendor(vendorId);
    }

    public List<Order> getAllOrdersAdmin() {
        return orderDao.findAllAdmin();
    }

    public Order getOrderDetails(Long orderId, UserDto user) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (user.getRole() == Role.CUSTOMER) {
            if (!order.getCustomerId().equals(user.getId())) {
                throw new ForbiddenException("Access denied: You do not own this order");
            }
        } else if (user.getRole() == Role.VENDOR) {
            boolean containsVendorItem = order.getItems().stream()
                    .anyMatch(item -> item.getVendorId().equals(user.getId()));
            if (!containsVendorItem) {
                throw new ForbiddenException("Access denied: Order contains no items for your vendor account");
            }
        }

        return order;
    }

    public boolean updateOrderStatusAdmin(Long orderId, OrderStatus status) {
        orderDao.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return orderDao.updateStatus(orderId, status);
    }

    private String generateOrderNumber() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String unique = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + dateStr + "-" + unique;
    }
}
