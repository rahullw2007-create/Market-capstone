#pragma once
#include "model/User.h"
#include <optional>
#include <string>

class UserRepository {
public:
    std::optional<User> findByEmail(const std::string& email);
    std::optional<User> findById(int id);
    int create(const std::string& name, const std::string& email,
               const std::string& password, const std::string& role);
    bool emailExists(const std::string& email);
    bool validatePassword(const std::string& email, const std::string& password);
};
