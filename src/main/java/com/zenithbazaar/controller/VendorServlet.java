package com.zenithbazaar.controller;

import com.zenithbazaar.dto.UserDto;
import com.zenithbazaar.exception.ValidationException;
import com.zenithbazaar.model.Order;
import com.zenithbazaar.model.Product;
import com.zenithbazaar.security.SessionUtil;
import com.zenithbazaar.service.OrderService;
import com.zenithbazaar.service.ProductService;
import com.zenithbazaar.utility.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/vendor/*")
public class VendorServlet extends BaseServlet {
    private final ProductService productService;
    private final OrderService orderService;

    public VendorServlet() {
        this(new ProductService(), new OrderService());
    }

    public VendorServlet(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto vendor = SessionUtil.getCurrentUser(req);
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/products".equals(pathInfo) || "/products/".equals(pathInfo)) {
                List<Product> products = productService.getVendorProducts(vendor.getId());
                sendSuccess(resp, products);
            } else if ("/orders".equals(pathInfo) || "/orders/".equals(pathInfo)) {
                List<Order> orders = orderService.getVendorOrders(vendor.getId());
                sendSuccess(resp, orders);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto vendor = SessionUtil.getCurrentUser(req);
        String pathInfo = req.getPathInfo();
        try {
            if ("/products".equals(pathInfo) || "/products/".equals(pathInfo)) {
                Product productReq = JsonUtil.parseRequestBody(req, Product.class);
                Product created = productService.createProduct(vendor.getId(), productReq);
                sendCreated(resp, created, "Product created successfully");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto vendor = SessionUtil.getCurrentUser(req);
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo != null && pathInfo.startsWith("/products/")) {
                Long productId = parseIdFromPath(req);
                if (productId == null) {
                    throw new ValidationException("Invalid product ID");
                }
                Product productReq = JsonUtil.parseRequestBody(req, Product.class);
                productReq.setId(productId);
                boolean updated = productService.updateVendorProduct(vendor.getId(), productReq);
                sendSuccess(resp, updated, "Product updated successfully");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto vendor = SessionUtil.getCurrentUser(req);
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo != null && pathInfo.startsWith("/products/")) {
                Long productId = parseIdFromPath(req);
                if (productId == null) {
                    throw new ValidationException("Invalid product ID");
                }
                boolean deactivated = productService.deactivateVendorProduct(vendor.getId(), productId);
                sendSuccess(resp, deactivated, "Product deactivated successfully");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }
}
