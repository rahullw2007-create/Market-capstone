#pragma once
#include <drogon/HttpController.h>

class OrderController : public drogon::HttpController<OrderController> {
public:
    METHOD_LIST_BEGIN
    ADD_METHOD_TO(OrderController::cart, "/api/cart", drogon::Get);
    ADD_METHOD_TO(OrderController::addCart, "/api/cart", drogon::Post);
    ADD_METHOD_TO(OrderController::checkout, "/api/orders", drogon::Post);
    METHOD_LIST_END

    void cart(const drogon::HttpRequestPtr&, std::function<void(const drogon::HttpResponsePtr&)>&&);
    void addCart(const drogon::HttpRequestPtr&, std::function<void(const drogon::HttpResponsePtr&)>&&);
    void checkout(const drogon::HttpRequestPtr&, std::function<void(const drogon::HttpResponsePtr&)>&&);
};
