#include "db/Database.h"
#include <fstream>
#include <sstream>
#include <iostream>

Database& Database::instance() {
    static Database db;
    return db;
}

bool Database::open(const std::string& file) {
    if (db_) return true;
    if (sqlite3_open(file.c_str(), &db_) != SQLITE_OK) {
        std::cerr << "SQLite open failed: " << sqlite3_errmsg(db_) << "\n";
        return false;
    }
    return execute("PRAGMA foreign_keys = ON;");
}

sqlite3* Database::handle() const {
    return db_;
}

bool Database::execute(const std::string& sql) {
    char* err = nullptr;
    const int rc = sqlite3_exec(db_, sql.c_str(), nullptr, nullptr, &err);
    if (rc != SQLITE_OK) {
        std::cerr << "SQLite error: " << (err ? err : "unknown") << "\n";
        sqlite3_free(err);
        return false;
    }
    return true;
}

Database::~Database() {
    if (db_) sqlite3_close(db_);
}
