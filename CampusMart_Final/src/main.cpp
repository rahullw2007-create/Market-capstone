#include <drogon/drogon.h>
#include "db/Database.h"
#include <fstream>
#include <sstream>

static bool initializeDatabase() {
    if (!Database::instance().open("campusmart.db")) return false;
    std::ifstream in("db/schema.sql");
    if (!in) return false;
    std::stringstream buffer; buffer << in.rdbuf();
    return Database::instance().execute(buffer.str());
}

int main() {
    if (!initializeDatabase()) {
        std::cerr << "Database initialization failed.\n";
        return 1;
    }

    drogon::app().setDocumentRoot("./public");
    drogon::app().setThreadNum(4);
    drogon::app().addListener("0.0.0.0", 8080);
    std::cout << "CampusMart running at http://localhost:8080\n";
    drogon::app().run();
    return 0;
}
