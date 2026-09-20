package com.zenithbazaar.controller;

import com.google.gson.JsonObject;
import com.zenithbazaar.dto.CartSummaryDto;
import com.zenithbazaar.dto.UserDto;
import com.zenithbazaar.exception.ValidationException;
import com.zenithbazaar.security.SessionUtil;
import com.zenithbazaar.service.CartService;
import com.zenithbazaar.utility.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/basket/*")
public class BasketServlet extends BaseServlet {
    private final CartService cartService;

    public BasketServlet() {
        this.cartService = new CartService();
    }

    public BasketServlet(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto user = SessionUtil.getCurrentUser(req);
        try {
            CartSummaryDto cart = cartService.getCartSummary(user.getId());
            sendSuccess(resp, cart);
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto user = SessionUtil.getCurrentUser(req);
        String pathInfo = req.getPathInfo();
        try {
            if ("/items".equals(pathInfo) || "/items/".equals(pathInfo)) {
                JsonObject json = JsonUtil.parseRequestBody(req, JsonObject.class);
                if (!json.has("productId") || !json.has("quantity")) {
                    throw new ValidationException("productId and quantity are required");
                }
                Long productId = json.get("productId").getAsLong();
                int quantity = json.get("quantity").getAsInt();

                cartService.addItemToCart(user.getId(), productId, quantity);
                CartSummaryDto cart = cartService.getCartSummary(user.getId());
                sendSuccess(resp, cart, "Item added to cart");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto user = SessionUtil.getCurrentUser(req);
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo != null && pathInfo.startsWith("/items/")) {
                Long productId = parseIdFromPath(req);
                if (productId == null) {
                    throw new ValidationException("Invalid product ID");
                }
                JsonObject json = JsonUtil.parseRequestBody(req, JsonObject.class);
                if (!json.has("quantity")) {
                    throw new ValidationException("quantity is required");
                }
                int quantity = json.get("quantity").getAsInt();

                cartService.updateCartItemQuantity(user.getId(), productId, quantity);
                CartSummaryDto cart = cartService.getCartSummary(user.getId());
                sendSuccess(resp, cart, "Cart updated");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto user = SessionUtil.getCurrentUser(req);
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                cartService.clearCart(user.getId());
                CartSummaryDto cart = cartService.getCartSummary(user.getId());
                sendSuccess(resp, cart, "Cart cleared");
            } else if (pathInfo.startsWith("/items/")) {
                Long productId = parseIdFromPath(req);
                if (productId == null) {
                    throw new ValidationException("Invalid product ID");
                }
                cartService.removeItemFromCart(user.getId(), productId);
                CartSummaryDto cart = cartService.getCartSummary(user.getId());
                sendSuccess(resp, cart, "Item removed from cart");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }
}
