package com.zenithbazaar.controller;

import com.zenithbazaar.model.Product;
import com.zenithbazaar.service.ProductService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/catalog/*")
public class CatalogServlet extends BaseServlet {
    private final ProductService productService;

    public CatalogServlet() {
        this.productService = new ProductService();
    }

    public CatalogServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/products".equals(pathInfo) || "/products/".equals(pathInfo)) {
                String search = req.getParameter("search");
                String category = req.getParameter("category");
                String sort = req.getParameter("sort");
                int page = parseInteger(req.getParameter("page"), 1);
                int pageSize = parseInteger(req.getParameter("pageSize"), 20);

                List<Product> catalog = productService.getCatalog(search, category, sort, page, pageSize);
                sendSuccess(resp, catalog);
            } else if (pathInfo.startsWith("/products/")) {
                Long productId = parseIdFromPath(req);
                if (productId != null) {
                    Product product = productService.getProductDetails(productId);
                    sendSuccess(resp, product);
                } else {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID");
                }
            } else if ("/categories".equals(pathInfo)) {
                List<String> categories = productService.getCategories();
                sendSuccess(resp, categories);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    private int parseInteger(String val, int defaultVal) {
        if (val == null) return defaultVal;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
