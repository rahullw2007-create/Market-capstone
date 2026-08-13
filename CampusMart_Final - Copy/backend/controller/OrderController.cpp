#include "controller/OrderController.h"
#include "service/AuthService.h"
#include "service/OrderService.h"
#include <drogon/drogon.h>

static OrderService orders;
static AuthService orderAuth;

static drogon::HttpResponsePtr oj(int code,const Json::Value& v){auto r=drogon::HttpResponse::newHttpJsonResponse(v);r->setStatusCode(static_cast<drogon::HttpStatusCode>(code));return r;}
static std::optional<User> buyer(const drogon::HttpRequestPtr& req){auto h=req->getHeader("Authorization");if(h.rfind("Bearer ",0)!=0)return std::nullopt;auto u=orderAuth.userFromToken(h.substr(7));if(!u||u->role!="buyer")return std::nullopt;return u;}

void OrderController::cart(const drogon::HttpRequestPtr& req,std::function<void(const drogon::HttpResponsePtr&)>&& cb){
    auto u=buyer(req);if(!u){Json::Value v;v["error"]="Buyer login required";cb(oj(403,v));return;}
    auto items=orders.cart(u->id);Json::Value arr(Json::arrayValue);double total=0;
    for(auto&i:items){Json::Value x;x["product_id"]=i.productId;x["name"]=i.name;x["price"]=i.price;x["stock"]=i.stock;x["quantity"]=i.quantity;x["subtotal"]=i.subtotal;arr.append(x);total+=i.subtotal;}
    Json::Value v;v["items"]=arr;v["total"]=total;cb(oj(200,v));
}
void OrderController::addCart(const drogon::HttpRequestPtr& req,std::function<void(const drogon::HttpResponsePtr&)>&& cb){
    auto u=buyer(req);if(!u){Json::Value v;v["error"]="Buyer login required";cb(oj(403,v));return;}
    auto b=req->getJsonObject();int product=(*b)["product_id"].asInt();int quantity=(*b)["quantity"].asInt();if(quantity<1)quantity=1;
    Json::Value v;if(orders.add(u->id,product,quantity)){v["message"]="Added to cart";cb(oj(200,v));}else{v["error"]="Could not add to cart";cb(oj(400,v));}
}
void OrderController::checkout(const drogon::HttpRequestPtr& req,std::function<void(const drogon::HttpResponsePtr&)>&& cb){
    auto u=buyer(req);if(!u){Json::Value v;v["error"]="Buyer login required";cb(oj(403,v));return;}
    auto items=orders.cart(u->id);if(items.empty()){Json::Value v;v["error"]="Cart is empty";cb(oj(400,v));return;}
    double total=0;for(auto&i:items){if(i.quantity>i.stock){Json::Value v;v["error"]="Not enough stock for "+i.name;cb(oj(400,v));return;}total+=i.subtotal;}
    int id=orders.place(u->id,total);Json::Value v;if(id<0){v["error"]="Could not place order";cb(oj(400,v));}else{v["message"]="Order placed. Mock payment successful.";v["order_id"]=id;v["total"]=total;v["payment_status"]="PAID";cb(oj(201,v));}
}
