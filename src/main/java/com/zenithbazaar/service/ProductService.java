package com.zenithbazaar.service;

import com.zenithbazaar.exception.ForbiddenException;
import com.zenithbazaar.exception.NotFoundException;
import com.zenithbazaar.exception.ValidationException;
import com.zenithbazaar.model.Product;
import com.zenithbazaar.repository.ProductDao;
import com.zenithbazaar.repository.ReviewDao;
import com.zenithbazaar.utility.ValidationUtil;

import java.util.List;

public class ProductService {
    private final ProductDao productDao;
    private final ReviewDao reviewDao;

    public ProductService() {
        this(new ProductDao(), new ReviewDao());
    }

    public ProductService(ProductDao productDao, ReviewDao reviewDao) {
        this.productDao = productDao;
        this.reviewDao = reviewDao;
    }

    public List<Product> getCatalog(String search, String category, String sort, int page, int pageSize) {
        int limit = Math.max(1, Math.min(pageSize, 100));
        int offset = Math.max(0, (page - 1) * limit);
        return productDao.findCatalog(search, category, sort, offset, limit);
    }

    public List<String> getCategories() {
        return productDao.getCategories();
    }

    public Product getProductDetails(Long id) {
        Product p = productDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        var reviews = reviewDao.findByProductId(id);
        double avgRating = reviews.stream().mapToInt(r -> r.getRating()).average().orElse(0.0);
        p.setAverageRating(Math.round(avgRating * 10.0) / 10.0);
        p.setReviewCount(reviews.size());
        return p;
    }

    public List<Product> getVendorProducts(Long vendorId) {
        return productDao.findByVendorId(vendorId);
    }

    public List<Product> getAllProductsAdmin() {
        return productDao.findAllAdmin();
    }

    public Product createProduct(Long vendorId, Product product) {
        if (product == null) {
            throw new ValidationException("Product data cannot be null");
        }
        ValidationUtil.requireNotBlank(product.getName(), "Product Name");
        ValidationUtil.requireNotBlank(product.getCategory(), "Category");
        ValidationUtil.validatePositivePrice(product.getPrice());
        ValidationUtil.validateNonNegativeQuantity(product.getQuantity());

        product.setVendorId(vendorId);
        product.setActive(true);
        return productDao.save(product);
    }

    public boolean updateVendorProduct(Long vendorId, Product product) {
        if (product == null || product.getId() == null) {
            throw new ValidationException("Invalid product update request");
        }
        Product existing = productDao.findById(product.getId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!existing.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("Unauthorized: You do not own this product");
        }

        ValidationUtil.requireNotBlank(product.getName(), "Product Name");
        ValidationUtil.requireNotBlank(product.getCategory(), "Category");
        ValidationUtil.validatePositivePrice(product.getPrice());
        ValidationUtil.validateNonNegativeQuantity(product.getQuantity());

        product.setVendorId(vendorId);
        return productDao.update(product);
    }

    public boolean deactivateVendorProduct(Long vendorId, Long productId) {
        Product existing = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!existing.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("Unauthorized: You do not own this product");
        }

        return productDao.updateStatus(productId, false);
    }

    public boolean updateProductStatusAdmin(Long productId, boolean active) {
        productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return productDao.updateStatus(productId, active);
    }
}
