#include "repository/ProductRepository.h"
#include "db/Database.h"
#include <sqlite3.h>

static Product rowToProduct(sqlite3_stmt* st) {
    return Product{
        sqlite3_column_int(st,0),
        sqlite3_column_int(st,1),
        reinterpret_cast<const char*>(sqlite3_column_text(st,2)),
        reinterpret_cast<const char*>(sqlite3_column_text(st,3)),
        reinterpret_cast<const char*>(sqlite3_column_text(st,4)),
        sqlite3_column_double(st,5),
        sqlite3_column_int(st,6),
        reinterpret_cast<const char*>(sqlite3_column_text(st,7)),
        reinterpret_cast<const char*>(sqlite3_column_text(st,8))
    };
}

std::vector<Product> ProductRepository::list(const std::string& search, const std::string& category) {
    std::vector<Product> out;
    sqlite3_stmt* st = nullptr;
    const char* sql =
        "SELECT p.id,p.seller_id,u.name,p.name,p.description,p.price,p.stock,p.category,p.image_url "
        "FROM products p JOIN users u ON u.id=p.seller_id "
        "WHERE (?='' OR p.name LIKE '%'||?||'%' OR p.description LIKE '%'||?||'%') "
        "AND (?='' OR p.category=?) ORDER BY p.id DESC;";
    if (sqlite3_prepare_v2(Database::instance().handle(), sql, -1, &st, nullptr) != SQLITE_OK) return out;
    sqlite3_bind_text(st,1,search.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_text(st,2,search.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_text(st,3,search.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_text(st,4,category.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_text(st,5,category.c_str(),-1,SQLITE_TRANSIENT);
    while (sqlite3_step(st) == SQLITE_ROW) out.push_back(rowToProduct(st));
    sqlite3_finalize(st);
    return out;
}

int ProductRepository::create(int sellerId, const Product& p) {
    sqlite3_stmt* st = nullptr;
    const char* sql = "INSERT INTO products(seller_id,name,description,price,stock,category,image_url) VALUES(?,?,?,?,?,?,?);";
    if (sqlite3_prepare_v2(Database::instance().handle(), sql, -1, &st, nullptr) != SQLITE_OK) return -1;
    sqlite3_bind_int(st,1,sellerId);
    sqlite3_bind_text(st,2,p.name.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_text(st,3,p.description.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_double(st,4,p.price);
    sqlite3_bind_int(st,5,p.stock);
    sqlite3_bind_text(st,6,p.category.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_text(st,7,p.imageUrl.c_str(),-1,SQLITE_TRANSIENT);
    const int rc=sqlite3_step(st);
    sqlite3_finalize(st);
    return rc==SQLITE_DONE ? static_cast<int>(sqlite3_last_insert_rowid(Database::instance().handle())) : -1;
}

bool ProductRepository::update(int sellerId, int id, const Product& p) {
    sqlite3_stmt* st=nullptr;
    const char* sql="UPDATE products SET name=?,description=?,price=?,stock=?,category=?,image_url=? WHERE id=? AND seller_id=?;";
    if(sqlite3_prepare_v2(Database::instance().handle(),sql,-1,&st,nullptr)!=SQLITE_OK) return false;
    sqlite3_bind_text(st,1,p.name.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_text(st,2,p.description.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_double(st,3,p.price);
    sqlite3_bind_int(st,4,p.stock);
    sqlite3_bind_text(st,5,p.category.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_text(st,6,p.imageUrl.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_int(st,7,id); sqlite3_bind_int(st,8,sellerId);
    const bool ok=sqlite3_step(st)==SQLITE_DONE && sqlite3_changes(Database::instance().handle())>0;
    sqlite3_finalize(st); return ok;
}

bool ProductRepository::remove(int sellerId,int id){
    sqlite3_stmt* st=nullptr;
    const char* sql="DELETE FROM products WHERE id=? AND seller_id=?;";
    if(sqlite3_prepare_v2(Database::instance().handle(),sql,-1,&st,nullptr)!=SQLITE_OK) return false;
    sqlite3_bind_int(st,1,id); sqlite3_bind_int(st,2,sellerId);
    const bool ok=sqlite3_step(st)==SQLITE_DONE && sqlite3_changes(Database::instance().handle())>0;
    sqlite3_finalize(st); return ok;
}

std::optional<Product> ProductRepository::find(int id){
    sqlite3_stmt* st=nullptr;
    const char* sql="SELECT p.id,p.seller_id,u.name,p.name,p.description,p.price,p.stock,p.category,p.image_url FROM products p JOIN users u ON u.id=p.seller_id WHERE p.id=?;";
    if(sqlite3_prepare_v2(Database::instance().handle(),sql,-1,&st,nullptr)!=SQLITE_OK) return std::nullopt;
    sqlite3_bind_int(st,1,id);
    std::optional<Product> out;
    if(sqlite3_step(st)==SQLITE_ROW) out=rowToProduct(st);
    sqlite3_finalize(st); return out;
}
