#pragma once
#include "repository/OrderRepository.h"

class OrderService {
public:
    std::vector<CartItem> cart(int buyerId){ return repo_.cart(buyerId); }
    bool add(int buyerId,int productId,int quantity){ return repo_.addToCart(buyerId,productId,quantity); }
    int place(int buyerId,double total){ return repo_.placeOrder(buyerId,total); }
private:
    OrderRepository repo_;
};
