#pragma once
#include "model/Product.h"
#include <vector>
#include <optional>
#include <string>

class ProductRepository {
public:
    std::vector<Product> list(const std::string& search, const std::string& category);
    int create(int sellerId, const Product& p);
    bool update(int sellerId, int id, const Product& p);
    bool remove(int sellerId, int id);
    std::optional<Product> find(int id);
};
