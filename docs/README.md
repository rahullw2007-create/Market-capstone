# Rahul Mart — Multi-Vendor Marketplace

A full-stack multi-vendor e-commerce marketplace built with Java 17, Servlets, JDBC, H2, and a responsive HTML/CSS/JavaScript frontend.

## 🌐 Live Demo

**Website:** https://rahul-mart.onrender.com

**GitHub:** https://github.com/rahullw2007-create/Market-capstone

Deployed on Render using Docker and Apache Tomcat 9.

## ✨ Features

### Customer
- Registration and login
- Product catalogue and product details
- Add, update, and remove cart items
- Server-side stock and price validation
- Checkout and order placement
- Order history and order details
- Reviews for eligible purchased products
- Session-based authentication
- Mock payment flow

### Vendor
- Vendor registration and login
- Vendor dashboard
- Add and edit products
- Update price and stock
- Activate/deactivate products
- View incoming customer orders
- Inventory management

### Administrator
- Administrator login
- View users, products, and orders
- Edit product information
- Update order status
- Marketplace administration

### Additional
- Role-based authorization
- BCrypt password hashing
- Persistent shopping cart
- Transactional checkout
- JSON API endpoints
- Responsive frontend
- Integrated chatbot
- Health/system endpoint
- Automated tests
- Docker deployment

## 🛠️ Technology Stack

**Backend:** Java 17, Java Servlets, JDBC, Maven, Apache Tomcat 9, Gson

**Database:** H2, HikariCP

**Security:** BCrypt, session authentication, role-based authorization, server-side validation

**Frontend:** HTML5, CSS3, JavaScript

**Deployment:** Docker, Render, Apache Tomcat 9

## 👥 User Roles

| Role | Main Responsibilities |
|---|---|
| Customer | Browse products, manage cart, place orders, review purchased products |
| Vendor | Manage products, stock, prices, and incoming orders |
| Administrator | Manage users, products, and orders |

## 🔐 Demo Accounts

All demo accounts use the password `Demo1234!`.

| Role | Email |
|---|---|
| Customer | `customer1@gmail.com` |
| Vendor | `vendor1@techstore.com` |
| Vendor | `vendor2@fashionhub.com` |
| Administrator | `admin@zenithbazaar.com` |

## 📦 Project Structure

```text
rahul-mart/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/zenithbazaar/
│   │   └── webapp/
│   │       ├── css/
│   │       ├── js/
│   │       ├── *.html
│   │       └── WEB-INF/
│   └── test/
├── docker/
│   └── start-rahulmart.sh
├── docs/
├── Dockerfile
├── pom.xml
└── README.md
```

## 🚀 Run Locally

### Requirements

- Java 17
- Maven 3.9+
- Apache Tomcat 9
- Git

### 1. Clone the repository

```bash
git clone https://github.com/rahullw2007-create/Market-capstone.git
cd Market-capstone
```

### 2. Build the application

```bash
mvn clean package
```

The generated WAR file will be:

```text
target/zenith-bazaar.war
```

### 3. Deploy to Tomcat

Copy the WAR file into Tomcat's `webapps` directory and start Tomcat.

Windows:

```text
bin\\startup.bat
```

Linux/macOS:

```bash
./bin/startup.sh
```

Then open:

```text
http://localhost:8080/zenith-bazaar/
```

## 🐳 Docker

Build the image:

```bash
docker build -t rahul-mart .
```

Run it:

```bash
docker run -p 8080:8080 rahul-mart
```

The Docker configuration deploys the WAR as `ROOT.war`, so the container serves the application from the root path.

## ☁️ Render Deployment

Deployment flow:

```text
GitHub → Render → Docker → Apache Tomcat 9 → Rahul Mart
```

Live application:

**https://rahul-mart.onrender.com**

The startup script automatically uses the port provided by the hosting environment.

## 🗄️ Database

The application uses H2 with HikariCP.

Main tables:

- `users`
- `products`
- `shopping_cart`
- `purchases`
- `purchase_items`
- `product_reviews`

## 🔌 Main API Areas

The backend provides JSON-based endpoints for:

- Account registration and login
- Product catalogue and product details
- Cart management
- Checkout and purchases
- Customer orders
- Vendor products and orders
- Administrator management
- Product reviews
- System/health information

## 🧪 Verification

The application has been tested locally and on the public deployment.

Verified flows include:

- Customer login and authentication
- Product browsing and product details
- Cart management
- Checkout and order creation
- Customer order history
- Vendor login and dashboard
- Vendor product management
- Vendor incoming orders
- Administrator login
- Administrator users, products, and orders pages
- Order status updates
- Logout/session behaviour
- Public Render deployment

## 🔒 Security

The application includes:

- BCrypt password hashing
- Session-based authentication
- Role-based authorization
- Server-side validation
- Server-side stock checks
- Server-side price checks
- Transactional order processing
- Protected vendor and administrator operations

## 📚 Documentation

Additional project documentation is available in the `docs/` directory.

## 👨‍💻 Project

**Rahul Mart — Multi-Vendor Marketplace**

Built using Java 17, Maven, Apache Tomcat, H2, HikariCP, HTML, CSS, JavaScript, and Docker.
