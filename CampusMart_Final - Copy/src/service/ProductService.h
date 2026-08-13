#pragma once
#include "repository/ProductRepository.h"

class ProductService {
public:
    std::vector<Product> list(const std::string& search,const std::string& category){ return repo_.list(search,category); }
    int add(int sellerId,const Product& p){ return repo_.create(sellerId,p); }
    bool update(int sellerId,int id,const Product& p){ return repo_.update(sellerId,id,p); }
    bool remove(int sellerId,int id){ return repo_.remove(sellerId,id); }
private:
    ProductRepository repo_;
};
