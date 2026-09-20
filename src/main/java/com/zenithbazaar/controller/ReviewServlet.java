package com.zenithbazaar.controller;

import com.zenithbazaar.dto.ReviewRequest;
import com.zenithbazaar.dto.UserDto;
import com.zenithbazaar.model.Review;
import com.zenithbazaar.security.SessionUtil;
import com.zenithbazaar.service.ReviewService;
import com.zenithbazaar.utility.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/reviews/*")
public class ReviewServlet extends BaseServlet {
    private final ReviewService reviewService;

    public ReviewServlet() {
        this.reviewService = new ReviewService();
    }

    public ReviewServlet(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo != null && pathInfo.startsWith("/product/")) {
                Long productId = parseIdFromPath(req);
                if (productId != null) {
                    List<Review> reviews = reviewService.getReviewsByProduct(productId);
                    sendSuccess(resp, reviews);
                } else {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID");
                }
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto user = SessionUtil.getCurrentUser(req);
        try {
            ReviewRequest reviewReq = JsonUtil.parseRequestBody(req, ReviewRequest.class);
            Review review = reviewService.createReview(user.getId(), reviewReq);
            sendCreated(resp, review, "Review submitted successfully");
        } catch (Exception e) {
            handleError(resp, e);
        }
    }
}
