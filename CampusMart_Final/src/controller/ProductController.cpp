#include "controller/ProductController.h"
#include <drogon/drogon.h>

static ProductService products;
static AuthService productAuth;

static drogon::HttpResponsePtr pj(int code,const Json::Value& v){
    auto r=drogon::HttpResponse::newHttpJsonResponse(v);r->setStatusCode(static_cast<drogon::HttpStatusCode>(code));return r;
}
static std::optional<User> current(const drogon::HttpRequestPtr& req){
    auto h=req->getHeader("Authorization");if(h.rfind("Bearer ",0)!=0)return std::nullopt;return productAuth.userFromToken(h.substr(7));
}
static Json::Value productJson(const Product& p){
    Json::Value v;v["id"]=p.id;v["seller_id"]=p.sellerId;v["seller_name"]=p.sellerName;v["name"]=p.name;v["description"]=p.description;v["price"]=p.price;v["stock"]=p.stock;v["category"]=p.category;v["image_url"]=p.imageUrl;return v;
}

void ProductController::list(const drogon::HttpRequestPtr& req,std::function<void(const drogon::HttpResponsePtr&)>&& cb){
    auto ps=products.list(req->getParameter("search"),req->getParameter("category"));Json::Value arr(Json::arrayValue);for(auto&p:ps)arr.append(productJson(p));Json::Value v;v["products"]=arr;cb(pj(200,v));
}
void ProductController::add(const drogon::HttpRequestPtr& req,std::function<void(const drogon::HttpResponsePtr&)>&& cb){
    auto u=current(req);if(!u||u->role!="seller"){Json::Value v;v["error"]="Seller login required";cb(pj(403,v));return;}
    auto b=req->getJsonObject();if(!b){Json::Value v;v["error"]="Invalid JSON";cb(pj(400,v));return;}
    Product p;p.name=(*b)["name"].asString();p.description=(*b)["description"].asString();p.price=(*b)["price"].asDouble();p.stock=(*b)["stock"].asInt();p.category=(*b)["category"].asString();p.imageUrl=(*b)["image_url"].asString();
    int id=products.add(u->id,p);Json::Value v;if(id<0){v["error"]="Could not add product";cb(pj(400,v));}else{v["message"]="Product added";v["id"]=id;cb(pj(201,v));}
}
void ProductController::update(const drogon::HttpRequestPtr& req,std::function<void(const drogon::HttpResponsePtr&)>&& cb,int id){
    auto u=current(req);if(!u||u->role!="seller"){Json::Value v;v["error"]="Seller login required";cb(pj(403,v));return;}
    auto b=req->getJsonObject();Product p;p.name=(*b)["name"].asString();p.description=(*b)["description"].asString();p.price=(*b)["price"].asDouble();p.stock=(*b)["stock"].asInt();p.category=(*b)["category"].asString();p.imageUrl=(*b)["image_url"].asString();
    Json::Value v;if(products.update(u->id,id,p)){v["message"]="Product updated";cb(pj(200,v));}else{v["error"]="Product not found or not yours";cb(pj(404,v));}
}
void ProductController::remove(const drogon::HttpRequestPtr& req,std::function<void(const drogon::HttpResponsePtr&)>&& cb,int id){
    auto u=current(req);if(!u||u->role!="seller"){Json::Value v;v["error"]="Seller login required";cb(pj(403,v));return;}
    Json::Value v;if(products.remove(u->id,id)){v["message"]="Product deleted";cb(pj(200,v));}else{v["error"]="Product not found or not yours";cb(pj(404,v));}
}
