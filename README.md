# CampusMart

A small campus marketplace capstone demo.

## Required functionality
- Register / Login
- Buyer and Seller roles
- Seller adds products
- Buyer browses products
- Search/filter products
- Add to cart
- View cart
- Place order
- Mock payment
- SQLite database with foreign keys
- C++20 + Drogon
- Controller -> Service -> Repository structure

## Project structure

backend/
  controller/
  service/
  repository/
  model/
db/schema.sql
public/
src/
CMakeLists.txt
vcpkg.json

## Windows setup

Use **Developer PowerShell for VS 2022** (not a normal PowerShell) so MSVC is available.

From the project folder:

```powershell
cmake --preset default
cmake --build build
.\build\CampusMart.exe
```

If the preset is not available, use:

```powershell
cmake -S . -B build -G "Visual Studio 17 2022" -A x64
cmake --build build --config Release
.\build\Release\CampusMart.exe
```

Open:

http://localhost:8080

The first run creates `campusmart.db` and enables SQLite foreign keys.

## vcpkg

If CMake cannot find Drogon/SQLite3, configure with your vcpkg toolchain file, for example:

```powershell
cmake -S . -B build -G "Visual Studio 17 2022" -A x64 -DCMAKE_TOOLCHAIN_FILE=C:\vcpkg\scripts\buildsystems\vcpkg.cmake
cmake --build build --config Release
```

Replace `C:\vcpkg` with your actual vcpkg location.

## Demo accounts

Register your own accounts from the website.

For a quick demo:
1. Register a seller.
2. Login as seller.
3. Add a product.
4. Logout.
5. Register/login as buyer.
6. Search the product, add it to cart and place the order.

## Notes

This is a capstone/demo implementation. Passwords are stored as entered for simplicity; for production, use a strong password hash such as Argon2/bcrypt/scrypt and persistent sessions.
