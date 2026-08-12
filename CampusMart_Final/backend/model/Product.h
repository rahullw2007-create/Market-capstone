#pragma once
#include <string>

struct Product {
    int id{};
    int sellerId{};
    std::string sellerName;
    std::string name;
    std::string description;
    double price{};
    int stock{};
    std::string category;
    std::string imageUrl;
};
