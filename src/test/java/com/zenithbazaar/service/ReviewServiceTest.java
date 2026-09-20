package com.zenithbazaar.service;

import com.zenithbazaar.TestDatabase;
import com.zenithbazaar.dto.ReviewRequest;
import com.zenithbazaar.exception.ForbiddenException;
import com.zenithbazaar.exception.ValidationException;
import com.zenithbazaar.model.Review;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewServiceTest {
    private static ReviewService reviewService;

    @BeforeAll
    static void setUp() {
        TestDatabase.setupInMemoryDatabase();
        reviewService = new ReviewService();
    }

    @Test
    void testPreventReviewWithoutDeliveredPurchase() {
        Long customerId = 5L; // Bob (no delivered order for Product 1)
        Long productId = 1L;

        ReviewRequest req = new ReviewRequest();
        req.setProductId(productId);
        req.setRating(5);
        req.setComment("Great product!");

        assertThrows(ForbiddenException.class, () -> reviewService.createReview(customerId, req));
    }

    @Test
    void testPreventDuplicateReviewForDeliveredProduct() {
        Long customerId = 4L; // Alice (already has seed review for Product 1)
        Long productId = 1L;

        ReviewRequest req = new ReviewRequest();
        req.setProductId(productId);
        req.setRating(4);
        req.setComment("Second review attempt");

        assertThrows(ValidationException.class, () -> reviewService.createReview(customerId, req));
    }
}
