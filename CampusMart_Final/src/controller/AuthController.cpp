#include "controller/AuthController.h"
#include <drogon/drogon.h>

static AuthService auth;

static drogon::HttpResponsePtr json(int code,const Json::Value& v){
    auto r=drogon::HttpResponse::newHttpJsonResponse(v); r->setStatusCode(static_cast<drogon::HttpStatusCode>(code)); return r;
}

static Json::Value userJson(const User& u){
    Json::Value v; v["id"]=u.id;v["name"]=u.name;v["email"]=u.email;v["role"]=u.role;return v;
}

void AuthController::registerUser(const drogon::HttpRequestPtr& req,std::function<void(const drogon::HttpResponsePtr&)>&& cb){
    auto b=req->getJsonObject(); if(!b){Json::Value v;v["error"]="Invalid JSON";cb(json(400,v));return;}
    auto r=auth.registerUser((*b)["name"].asString(),(*b)["email"].asString(),(*b)["password"].asString(),(*b)["role"].asString());
    Json::Value v;v["message"]=r.message;
    if(r.ok){v["token"]=r.token;v["user"]=userJson(r.user);cb(json(201,v));}else{v["error"]=r.message;cb(json(400,v));}
}

void AuthController::login(const drogon::HttpRequestPtr& req,std::function<void(const drogon::HttpResponsePtr&)>&& cb){
    auto b=req->getJsonObject(); if(!b){Json::Value v;v["error"]="Invalid JSON";cb(json(400,v));return;}
    auto r=auth.login((*b)["email"].asString(),(*b)["password"].asString());
    Json::Value v;v["message"]=r.message;
    if(r.ok){v["token"]=r.token;v["user"]=userJson(r.user);cb(json(200,v));}else{v["error"]=r.message;cb(json(401,v));}
}

void AuthController::me(const drogon::HttpRequestPtr& req,std::function<void(const drogon::HttpResponsePtr&)>&& cb){
    auto h=req->getHeader("Authorization"); if(h.rfind("Bearer ",0)!=0){Json::Value v;v["error"]="Missing token";cb(json(401,v));return;}
    auto u=auth.userFromToken(h.substr(7)); if(!u){Json::Value v;v["error"]="Invalid token";cb(json(401,v));return;}
    Json::Value v;v["user"]=userJson(*u);cb(json(200,v));
}
