package com.zenithbazaar.service;

import com.zenithbazaar.dto.CartSummaryDto;
import com.zenithbazaar.exception.NotFoundException;
import com.zenithbazaar.exception.ValidationException;
import com.zenithbazaar.model.CartItem;
import com.zenithbazaar.model.Product;
import com.zenithbazaar.repository.CartDao;
import com.zenithbazaar.repository.ProductDao;
import com.zenithbazaar.utility.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;

public class CartService {
    private final CartDao cartDao;
    private final ProductDao productDao;

    public CartService() {
        this(new CartDao(), new ProductDao());
    }

    public CartService(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    public CartSummaryDto getCartSummary(Long customerId) {
        List<CartItem> items = cartDao.findByCustomerId(customerId);
        CartSummaryDto summary = new CartSummaryDto();
        BigDecimal subtotal = BigDecimal.ZERO;
        int count = 0;

        for (CartItem item : items) {
            subtotal = subtotal.add(item.getSubtotal());
            count += item.getQuantity();
        }

        summary.setItems(items);
        summary.setSubtotal(subtotal);
        summary.setTotal(subtotal); // No tax/shipping for simplicity, or calculated on server
        summary.setTotalItemsCount(count);
        return summary;
    }

    public void addItemToCart(Long customerId, Long productId, int quantity) {
        ValidationUtil.validatePositiveQuantity(quantity);

        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new ValidationException("Product is currently unavailable");
        }

        int currentCartQty = cartDao.findByCustomerAndProduct(customerId, productId)
                .map(CartItem::getQuantity)
                .orElse(0);

        int totalRequested = currentCartQty + quantity;
        if (totalRequested > product.getQuantity()) {
            throw new ValidationException("Cannot add requested quantity. Available stock: " + product.getQuantity());
        }

        boolean success = cartDao.addOrUpdateItem(customerId, productId, quantity);
        if (!success) {
            throw new RuntimeException("Failed to update cart");
        }
    }

    public void updateCartItemQuantity(Long customerId, Long productId, int quantity) {
        if (quantity <= 0) {
            cartDao.removeItem(customerId, productId);
            return;
        }

        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new ValidationException("Product is inactive");
        }

        if (quantity > product.getQuantity()) {
            throw new ValidationException("Requested quantity exceeds available stock (" + product.getQuantity() + ")");
        }

        cartDao.updateQuantity(customerId, productId, quantity);
    }

    public void removeItemFromCart(Long customerId, Long productId) {
        cartDao.removeItem(customerId, productId);
    }

    public void clearCart(Long customerId) {
        cartDao.clearCart(customerId);
    }
}
