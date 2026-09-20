package com.zenithbazaar.controller;

import com.zenithbazaar.dto.CheckoutRequest;
import com.zenithbazaar.dto.UserDto;
import com.zenithbazaar.model.Order;
import com.zenithbazaar.security.SessionUtil;
import com.zenithbazaar.service.OrderService;
import com.zenithbazaar.utility.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/purchases/*")
public class PurchaseServlet extends BaseServlet {
    private final OrderService orderService;

    public PurchaseServlet() {
        this.orderService = new OrderService();
    }

    public PurchaseServlet(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto user = SessionUtil.getCurrentUser(req);
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                List<Order> orders = orderService.getCustomerOrders(user.getId());
                sendSuccess(resp, orders);
            } else {
                Long orderId = parseIdFromPath(req);
                if (orderId != null) {
                    Order order = orderService.getOrderDetails(orderId, user);
                    sendSuccess(resp, order);
                } else {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID");
                }
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto user = SessionUtil.getCurrentUser(req);
        String pathInfo = req.getPathInfo();
        try {
            if ("/checkout".equals(pathInfo) || "/checkout/".equals(pathInfo)) {
                CheckoutRequest checkoutReq = null;
                try {
                    checkoutReq = JsonUtil.parseRequestBody(req, CheckoutRequest.class);
                } catch (Exception ignored) {
                    checkoutReq = new CheckoutRequest();
                }
                Order createdOrder = orderService.checkoutTransactional(user.getId(), checkoutReq);
                sendCreated(resp, createdOrder, "Checkout completed successfully");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }
}
