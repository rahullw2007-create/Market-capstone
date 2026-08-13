#include "service/AuthService.h"
#include <random>
#include <unordered_map>
#include <mutex>

static std::unordered_map<std::string,int> sessions;
static std::mutex sessionsMutex;

static std::string token() {
    static std::mt19937_64 rng{std::random_device{}()};
    std::uniform_int_distribution<unsigned long long> d;
    return std::to_string(d(rng)) + std::to_string(d(rng));
}

AuthResult AuthService::registerUser(const std::string& name,const std::string& email,const std::string& password,const std::string& role){
    if(name.empty()||email.empty()||password.empty()) return {false,"All fields are required","","{}"};
    if(role!="buyer" && role!="seller") return {false,"Role must be buyer or seller","","{}"};
    if(users_.emailExists(email)) return {false,"Email already registered","","{}"};
    int id=users_.create(name,email,password,role);
    if(id<0) return {false,"Could not create user","","{}"};
    auto u=users_.findById(id);
    std::string t=token();
    {std::lock_guard<std::mutex> lock(sessionsMutex); sessions[t]=id;}
    return {true,"Registration successful",t,u.value()};
}

AuthResult AuthService::login(const std::string& email,const std::string& password){
    if(!users_.validatePassword(email,password)) return {false,"Invalid email or password","","{}"};
    auto u=users_.findByEmail(email);
    std::string t=token();
    {std::lock_guard<std::mutex> lock(sessionsMutex); sessions[t]=u->id;}
    return {true,"Login successful",t,*u};
}

std::optional<User> AuthService::userFromToken(const std::string& tokenValue){
    std::lock_guard<std::mutex> lock(sessionsMutex);
    auto it=sessions.find(tokenValue);
    if(it==sessions.end()) return std::nullopt;
    return users_.findById(it->second);
}
