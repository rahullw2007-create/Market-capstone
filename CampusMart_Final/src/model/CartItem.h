#pragma once
#include <string>

struct CartItem {
    int productId{};
    std::string name;
    double price{};
    int stock{};
    int quantity{};
    double subtotal{};
};
