#pragma once
#include "model/CartItem.h"
#include <vector>

class OrderRepository {
public:
    std::vector<CartItem> cart(int buyerId);
    bool addToCart(int buyerId, int productId, int quantity);
    bool clearCart(int buyerId);
    int placeOrder(int buyerId, double total);
};
