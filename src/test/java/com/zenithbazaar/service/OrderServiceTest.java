package com.zenithbazaar.service;

import com.zenithbazaar.TestDatabase;
import com.zenithbazaar.dto.CheckoutRequest;
import com.zenithbazaar.dto.UserDto;
import com.zenithbazaar.exception.ForbiddenException;
import com.zenithbazaar.model.Order;
import com.zenithbazaar.model.Product;
import com.zenithbazaar.model.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {
    private static OrderService orderService;
    private static CartService cartService;
    private static ProductService productService;

    @BeforeAll
    static void setUp() {
        TestDatabase.setupInMemoryDatabase();
        orderService = new OrderService();
        cartService = new CartService();
        productService = new ProductService();
    }

    @Test
    void testCheckoutTransactionAndInventoryReduction() {
        Long customerId = 6L; // Charlie
        Long productId = 4L;  // Smart Watch, Stock initial 30

        Product initialProd = productService.getProductDetails(productId);
        int initialStock = initialProd.getQuantity();

        cartService.clearCart(customerId);
        cartService.addItemToCart(customerId, productId, 3);

        CheckoutRequest req = new CheckoutRequest();
        req.setPaymentMethod("SIMULATED_CARD");
        req.setShippingAddress("123 Main St, New York");

        Order order = orderService.checkoutTransactional(customerId, req);
        assertNotNull(order.getId());
        assertNotNull(order.getOrderNumber());
        assertEquals(3, order.getItems().get(0).getQuantity());

        // Verify stock reduced by 3
        Product updatedProd = productService.getProductDetails(productId);
        assertEquals(initialStock - 3, updatedProd.getQuantity());

        // Verify cart is emptied
        assertTrue(cartService.getCartSummary(customerId).getItems().isEmpty());
    }

    @Test
    void testCustomerOrderIsolation() {
        Long customer1Id = 4L;
        Long customer2Id = 5L;

        var orders = orderService.getCustomerOrders(customer1Id);
        assertFalse(orders.isEmpty());
        Order c1Order = orders.get(0);

        UserDto customer2User = new UserDto();
        customer2User.setId(customer2Id);
        customer2User.setRole(Role.CUSTOMER);

        assertThrows(ForbiddenException.class, () -> orderService.getOrderDetails(c1Order.getId(), customer2User));
    }
}
