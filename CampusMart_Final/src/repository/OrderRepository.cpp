#include "repository/OrderRepository.h"
#include "db/Database.h"
#include <sqlite3.h>

std::vector<CartItem> OrderRepository::cart(int buyerId) {
    std::vector<CartItem> out;
    sqlite3_stmt* st=nullptr;
    const char* sql=
      "SELECT p.id,p.name,p.price,p.stock,ci.quantity,p.price*ci.quantity "
      "FROM carts c JOIN cart_items ci ON ci.cart_id=c.id JOIN products p ON p.id=ci.product_id "
      "WHERE c.buyer_id=?;";
    if(sqlite3_prepare_v2(Database::instance().handle(),sql,-1,&st,nullptr)!=SQLITE_OK) return out;
    sqlite3_bind_int(st,1,buyerId);
    while(sqlite3_step(st)==SQLITE_ROW){
        out.push_back(CartItem{
            sqlite3_column_int(st,0),
            reinterpret_cast<const char*>(sqlite3_column_text(st,1)),
            sqlite3_column_double(st,2),
            sqlite3_column_int(st,3),
            sqlite3_column_int(st,4),
            sqlite3_column_double(st,5)
        });
    }
    sqlite3_finalize(st); return out;
}

bool OrderRepository::addToCart(int buyerId,int productId,int quantity){
    sqlite3_stmt* st=nullptr;
    const char* sql1="INSERT OR IGNORE INTO carts(buyer_id) VALUES(?);";
    if(sqlite3_prepare_v2(Database::instance().handle(),sql1,-1,&st,nullptr)!=SQLITE_OK) return false;
    sqlite3_bind_int(st,1,buyerId); sqlite3_step(st); sqlite3_finalize(st);

    const char* sql2="INSERT INTO cart_items(cart_id,product_id,quantity) SELECT id,?,? FROM carts WHERE buyer_id=? ON CONFLICT(cart_id,product_id) DO UPDATE SET quantity=quantity+excluded.quantity;";
    if(sqlite3_prepare_v2(Database::instance().handle(),sql2,-1,&st,nullptr)!=SQLITE_OK) return false;
    sqlite3_bind_int(st,1,productId); sqlite3_bind_int(st,2,quantity); sqlite3_bind_int(st,3,buyerId);
    const bool ok=sqlite3_step(st)==SQLITE_DONE; sqlite3_finalize(st); return ok;
}

bool OrderRepository::clearCart(int buyerId){
    sqlite3_stmt* st=nullptr;
    const char* sql="DELETE FROM cart_items WHERE cart_id=(SELECT id FROM carts WHERE buyer_id=?);";
    if(sqlite3_prepare_v2(Database::instance().handle(),sql,-1,&st,nullptr)!=SQLITE_OK) return false;
    sqlite3_bind_int(st,1,buyerId); const bool ok=sqlite3_step(st)==SQLITE_DONE; sqlite3_finalize(st); return ok;
}

int OrderRepository::placeOrder(int buyerId,double total){
    if(!Database::instance().execute("BEGIN TRANSACTION;")) return -1;
    sqlite3_stmt* st=nullptr;
    const char* sql="INSERT INTO orders(buyer_id,total,payment_status,status) VALUES(?,?, 'PAID','PLACED');";
    if(sqlite3_prepare_v2(Database::instance().handle(),sql,-1,&st,nullptr)!=SQLITE_OK){ Database::instance().execute("ROLLBACK;"); return -1; }
    sqlite3_bind_int(st,1,buyerId); sqlite3_bind_double(st,2,total);
    if(sqlite3_step(st)!=SQLITE_DONE){sqlite3_finalize(st);Database::instance().execute("ROLLBACK;");return -1;}
    const int orderId=static_cast<int>(sqlite3_last_insert_rowid(Database::instance().handle()));
    sqlite3_finalize(st);

    const char* items="INSERT INTO order_items(order_id,product_id,quantity,unit_price) SELECT ?,ci.product_id,ci.quantity,p.price FROM cart_items ci JOIN carts c ON c.id=ci.cart_id JOIN products p ON p.id=ci.product_id WHERE c.buyer_id=?;";
    if(sqlite3_prepare_v2(Database::instance().handle(),items,-1,&st,nullptr)!=SQLITE_OK){Database::instance().execute("ROLLBACK;");return -1;}
    sqlite3_bind_int(st,1,orderId);sqlite3_bind_int(st,2,buyerId);
    if(sqlite3_step(st)!=SQLITE_DONE){sqlite3_finalize(st);Database::instance().execute("ROLLBACK;");return -1;}
    sqlite3_finalize(st);

    if(!clearCart(buyerId) || !Database::instance().execute("COMMIT;")) { Database::instance().execute("ROLLBACK;"); return -1; }
    return orderId;
}
