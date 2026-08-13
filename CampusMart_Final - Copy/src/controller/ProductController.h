#pragma once
#include <drogon/HttpController.h>
#include "service/AuthService.h"
#include "service/ProductService.h"

class ProductController : public drogon::HttpController<ProductController> {
public:
    METHOD_LIST_BEGIN
    ADD_METHOD_TO(ProductController::list, "/api/products", drogon::Get);
    ADD_METHOD_TO(ProductController::add, "/api/products", drogon::Post);
    ADD_METHOD_TO(ProductController::update, "/api/products/{1}", drogon::Put);
    ADD_METHOD_TO(ProductController::remove, "/api/products/{1}", drogon::Delete);
    METHOD_LIST_END

    void list(const drogon::HttpRequestPtr&, std::function<void(const drogon::HttpResponsePtr&)>&&);
    void add(const drogon::HttpRequestPtr&, std::function<void(const drogon::HttpResponsePtr&)>&&);
    void update(const drogon::HttpRequestPtr&, std::function<void(const drogon::HttpResponsePtr&)>&&, int id);
    void remove(const drogon::HttpRequestPtr&, std::function<void(const drogon::HttpResponsePtr&)>&&, int id);
};
