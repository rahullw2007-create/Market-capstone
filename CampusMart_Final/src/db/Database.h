#pragma once
#include <sqlite3.h>
#include <string>

class Database {
public:
    static Database& instance();
    bool open(const std::string& file = "campusmart.db");
    sqlite3* handle() const;
    bool execute(const std::string& sql);
    ~Database();

private:
    Database() = default;
    Database(const Database&) = delete;
    Database& operator=(const Database&) = delete;
    sqlite3* db_ = nullptr;
};
