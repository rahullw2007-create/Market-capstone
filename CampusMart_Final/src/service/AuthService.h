#pragma once
#include "model/User.h"
#include "repository/UserRepository.h"
#include <optional>
#include <string>

struct AuthResult {
    bool ok;
    std::string message;
    std::string token;
    User user;
};

class AuthService {
public:
    AuthResult registerUser(const std::string& name,const std::string& email,const std::string& password,const std::string& role);
    AuthResult login(const std::string& email,const std::string& password);
    std::optional<User> userFromToken(const std::string& token);
private:
    UserRepository users_;
};
