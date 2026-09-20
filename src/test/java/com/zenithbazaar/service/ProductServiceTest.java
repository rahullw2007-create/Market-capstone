package com.zenithbazaar.service;

import com.zenithbazaar.TestDatabase;
import com.zenithbazaar.exception.ForbiddenException;
import com.zenithbazaar.model.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {
    private static ProductService productService;

    @BeforeAll
    static void setUp() {
        TestDatabase.setupInMemoryDatabase();
        productService = new ProductService();
    }

    @Test
    void testCatalogBrowsing() {
        List<Product> products = productService.getCatalog(null, null, null, 1, 10);
        assertFalse(products.isEmpty());
    }

    @Test
    void testVendorProductCreationAndOwnership() {
        Long vendor1Id = 2L;
        Product p = new Product();
        p.setName("Vendor 1 Unique Laptop");
        p.setDescription("High performance gaming laptop");
        p.setPrice(new BigDecimal("1299.99"));
        p.setQuantity(5);
        p.setCategory("Electronics");
        p.setImageUrl("https://example.com/laptop.jpg");

        Product created = productService.createProduct(vendor1Id, p);
        assertNotNull(created.getId());
        assertEquals(vendor1Id, created.getVendorId());

        // Verify Vendor 2 CANNOT edit Vendor 1's product
        Long vendor2Id = 3L;
        created.setName("Hacked Laptop Name");
        assertThrows(ForbiddenException.class, () -> productService.updateVendorProduct(vendor2Id, created));
    }
}
