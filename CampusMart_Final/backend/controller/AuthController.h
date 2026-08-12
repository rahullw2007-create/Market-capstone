#pragma once
#include <drogon/HttpController.h>
#include "service/AuthService.h"

class AuthController : public drogon::HttpController<AuthController> {
public:
    METHOD_LIST_BEGIN
    ADD_METHOD_TO(AuthController::registerUser, "/api/auth/register", drogon::Post);
    ADD_METHOD_TO(AuthController::login, "/api/auth/login", drogon::Post);
    ADD_METHOD_TO(AuthController::me, "/api/auth/me", drogon::Get);
    METHOD_LIST_END

    void registerUser(const drogon::HttpRequestPtr&, std::function<void(const drogon::HttpResponsePtr&)>&&);
    void login(const drogon::HttpRequestPtr&, std::function<void(const drogon::HttpResponsePtr&)>&&);
    void me(const drogon::HttpRequestPtr&, std::function<void(const drogon::HttpResponsePtr&)>&&);
};
