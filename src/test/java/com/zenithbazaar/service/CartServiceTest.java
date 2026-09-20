package com.zenithbazaar.service;

import com.zenithbazaar.TestDatabase;
import com.zenithbazaar.dto.CartSummaryDto;
import com.zenithbazaar.exception.ValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {
    private static CartService cartService;

    @BeforeAll
    static void setUp() {
        TestDatabase.setupInMemoryDatabase();
        cartService = new CartService();
    }

    @Test
    void testAddToCartAndCalculateTotal() {
        Long customerId = 5L; // Customer 2 (Bob)
        Long productId = 1L;  // Wireless Headphones ($199.99, Stock 25)

        cartService.clearCart(customerId);
        cartService.addItemToCart(customerId, productId, 2);

        CartSummaryDto summary = cartService.getCartSummary(customerId);
        assertEquals(1, summary.getItems().size());
        assertEquals(2, summary.getTotalItemsCount());
        assertEquals(0, new java.math.BigDecimal("399.98").compareTo(summary.getTotal()));
    }

    @Test
    void testPreventExceedingStock() {
        Long customerId = 5L;
        Long productId = 2L; // Mechanical Keyboard, Stock 15

        assertThrows(ValidationException.class, () -> cartService.addItemToCart(customerId, productId, 999));
    }
}
