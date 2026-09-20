package com.zenithbazaar.service;

import com.zenithbazaar.dto.ReviewRequest;
import com.zenithbazaar.exception.ForbiddenException;
import com.zenithbazaar.exception.NotFoundException;
import com.zenithbazaar.exception.ValidationException;
import com.zenithbazaar.model.Product;
import com.zenithbazaar.model.Review;
import com.zenithbazaar.repository.OrderDao;
import com.zenithbazaar.repository.ProductDao;
import com.zenithbazaar.repository.ReviewDao;
import com.zenithbazaar.utility.ValidationUtil;

import java.util.List;

public class ReviewService {
    private final ReviewDao reviewDao;
    private final OrderDao orderDao;
    private final ProductDao productDao;

    public ReviewService() {
        this(new ReviewDao(), new OrderDao(), new ProductDao());
    }

    public ReviewService(ReviewDao reviewDao, OrderDao orderDao, ProductDao productDao) {
        this.reviewDao = reviewDao;
        this.orderDao = orderDao;
        this.productDao = productDao;
    }

    public Review createReview(Long customerId, ReviewRequest req) {
        if (req == null || req.getProductId() == null) {
            throw new ValidationException("Invalid review request");
        }
        ValidationUtil.validateRating(req.getRating());
        ValidationUtil.requireNotBlank(req.getComment(), "Review comment");
        ValidationUtil.validateMinLength(req.getComment(), 5, "Review comment");

        Product product = productDao.findById(req.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        // 1. Enforce delivered purchase check on SERVER
        boolean isEligible = orderDao.hasCustomerPurchasedProduct(customerId, req.getProductId());
        if (!isEligible) {
            throw new ForbiddenException("You can only review products from orders that have been delivered to you.");
        }

        // 2. Prevent duplicate reviews
        if (reviewDao.hasUserReviewedProduct(customerId, req.getProductId())) {
            throw new ValidationException("You have already reviewed this product.");
        }

        Review review = new Review();
        review.setProductId(req.getProductId());
        review.setCustomerId(customerId);
        review.setRating(req.getRating());
        review.setComment(req.getComment().trim());

        return reviewDao.save(review);
    }

    public List<Review> getReviewsByProduct(Long productId) {
        return reviewDao.findByProductId(productId);
    }
}
