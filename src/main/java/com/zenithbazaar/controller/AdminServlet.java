package com.zenithbazaar.controller;

import com.google.gson.JsonObject;
import com.zenithbazaar.dto.UserDto;
import com.zenithbazaar.exception.ValidationException;
import com.zenithbazaar.model.Order;
import com.zenithbazaar.model.OrderStatus;
import com.zenithbazaar.model.Product;
import com.zenithbazaar.security.SessionUtil;
import com.zenithbazaar.service.OrderService;
import com.zenithbazaar.service.ProductService;
import com.zenithbazaar.service.UserService;
import com.zenithbazaar.utility.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/administrator/*")
public class AdminServlet extends BaseServlet {
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    public AdminServlet() {
        this(new UserService(), new ProductService(), new OrderService());
    }

    public AdminServlet(UserService userService, ProductService productService, OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if ("/users".equals(pathInfo) || "/users/".equals(pathInfo)) {
                List<UserDto> users = userService.getAllUsers();
                sendSuccess(resp, users);
            } else if ("/products".equals(pathInfo) || "/products/".equals(pathInfo)) {
                List<Product> products = productService.getAllProductsAdmin();
                sendSuccess(resp, products);
            } else if ("/orders".equals(pathInfo) || "/orders/".equals(pathInfo)) {
                List<Order> orders = orderService.getAllOrdersAdmin();
                sendSuccess(resp, orders);
            } else if (pathInfo != null && pathInfo.startsWith("/orders/")) {
                Long orderId = parseIdFromPath(req);
                if (orderId == null) {
                    throw new ValidationException("Invalid order ID");
                }
                UserDto adminUser = SessionUtil.getCurrentUser(req);
                Order order = orderService.getOrderDetails(orderId, adminUser);
                sendSuccess(resp, order);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo != null && pathInfo.startsWith("/users/") && pathInfo.endsWith("/status")) {
                Long userId = parseIdFromPath(req);
                JsonObject json = JsonUtil.parseRequestBody(req, JsonObject.class);
                boolean active = json.get("active").getAsBoolean();
                boolean updated = userService.updateUserStatus(userId, active);
                sendSuccess(resp, updated, "User status updated");
            } else if (pathInfo != null && pathInfo.startsWith("/products/") && pathInfo.endsWith("/status")) {
                Long productId = parseIdFromPath(req);
                JsonObject json = JsonUtil.parseRequestBody(req, JsonObject.class);
                boolean active = json.get("active").getAsBoolean();
                boolean updated = productService.updateProductStatusAdmin(productId, active);
                sendSuccess(resp, updated, "Product status updated");
            } else if (pathInfo != null && pathInfo.startsWith("/orders/") && pathInfo.endsWith("/status")) {
                Long orderId = parseIdFromPath(req);
                JsonObject json = JsonUtil.parseRequestBody(req, JsonObject.class);
                String statusStr = json.get("status").getAsString();
                OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
                boolean updated = orderService.updateOrderStatusAdmin(orderId, status);
                sendSuccess(resp, updated, "Order status updated");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }
}
