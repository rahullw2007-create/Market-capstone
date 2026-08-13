#include "repository/UserRepository.h"
#include "db/Database.h"
#include <sqlite3.h>

std::optional<User> UserRepository::findByEmail(const std::string& email) {
    sqlite3_stmt* st = nullptr;
    const char* sql = "SELECT id,name,email,role FROM users WHERE email=?;";
    if (sqlite3_prepare_v2(Database::instance().handle(), sql, -1, &st, nullptr) != SQLITE_OK) return std::nullopt;
    sqlite3_bind_text(st, 1, email.c_str(), -1, SQLITE_TRANSIENT);

    std::optional<User> result;
    if (sqlite3_step(st) == SQLITE_ROW) {
        result = User{
            sqlite3_column_int(st, 0),
            reinterpret_cast<const char*>(sqlite3_column_text(st, 1)),
            reinterpret_cast<const char*>(sqlite3_column_text(st, 2)),
            reinterpret_cast<const char*>(sqlite3_column_text(st, 3))
        };
    }
    sqlite3_finalize(st);
    return result;
}

std::optional<User> UserRepository::findById(int id) {
    sqlite3_stmt* st = nullptr;
    const char* sql = "SELECT id,name,email,role FROM users WHERE id=?;";
    if (sqlite3_prepare_v2(Database::instance().handle(), sql, -1, &st, nullptr) != SQLITE_OK) return std::nullopt;
    sqlite3_bind_int(st, 1, id);
    std::optional<User> result;
    if (sqlite3_step(st) == SQLITE_ROW) {
        result = User{
            sqlite3_column_int(st, 0),
            reinterpret_cast<const char*>(sqlite3_column_text(st, 1)),
            reinterpret_cast<const char*>(sqlite3_column_text(st, 2)),
            reinterpret_cast<const char*>(sqlite3_column_text(st, 3))
        };
    }
    sqlite3_finalize(st);
    return result;
}

bool UserRepository::emailExists(const std::string& email) {
    return findByEmail(email).has_value();
}

int UserRepository::create(const std::string& name, const std::string& email,
                           const std::string& password, const std::string& role) {
    sqlite3_stmt* st = nullptr;
    const char* sql = "INSERT INTO users(name,email,password,role) VALUES(?,?,?,?);";
    if (sqlite3_prepare_v2(Database::instance().handle(), sql, -1, &st, nullptr) != SQLITE_OK) return -1;
    sqlite3_bind_text(st, 1, name.c_str(), -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(st, 2, email.c_str(), -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(st, 3, password.c_str(), -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(st, 4, role.c_str(), -1, SQLITE_TRANSIENT);
    const int rc = sqlite3_step(st);
    sqlite3_finalize(st);
    return rc == SQLITE_DONE ? static_cast<int>(sqlite3_last_insert_rowid(Database::instance().handle())) : -1;
}

bool UserRepository::validatePassword(const std::string& email, const std::string& password) {
    sqlite3_stmt* st = nullptr;
    const char* sql = "SELECT 1 FROM users WHERE email=? AND password=?;";
    if (sqlite3_prepare_v2(Database::instance().handle(), sql, -1, &st, nullptr) != SQLITE_OK) return false;
    sqlite3_bind_text(st, 1, email.c_str(), -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(st, 2, password.c_str(), -1, SQLITE_TRANSIENT);
    const bool ok = sqlite3_step(st) == SQLITE_ROW;
    sqlite3_finalize(st);
    return ok;
}
