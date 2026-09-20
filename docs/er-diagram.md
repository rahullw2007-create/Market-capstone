# ZenithBazaar - Entity Relationship (ER) Diagram

```mermaid
erDiagram
    USERS ||--o{ PRODUCTS : "vendor owns"
    USERS ||--o{ SHOPPING_CART : "customer owns"
    USERS ||--o{ PURCHASES : "customer places"
    USERS ||--o{ PRODUCT_REVIEWS : "customer writes"
    PRODUCTS ||--o{ SHOPPING_CART : "in cart"
    PRODUCTS ||--o{ PURCHASE_ITEMS : "in purchase"
    PRODUCTS ||--o{ PRODUCT_REVIEWS : "has reviews"
    PURCHASES ||--|{ PURCHASE_ITEMS : "contains"
    USERS ||--o{ PURCHASE_ITEMS : "vendor fulfills"

    USERS {
        bigint id PK
        string email UK
        string password_hash
        string full_name
        string role "CUSTOMER | VENDOR | ADMINISTRATOR"
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }

    PRODUCTS {
        bigint id PK
        bigint vendor_id FK
        string name
        string description
        decimal price "DECIMAL(10,2)"
        int quantity
        string category
        string image_url
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }

    SHOPPING_CART {
        bigint id PK
        bigint customer_id FK
        bigint product_id FK
        int quantity
        timestamp created_at
        timestamp updated_at
    }

    PURCHASES {
        bigint id PK
        string order_number UK
        bigint customer_id FK
        decimal total_amount "DECIMAL(10,2)"
        string status "NEW | CONFIRMED | PROCESSING | SHIPPED | DELIVERED | CANCELLED"
        timestamp created_at
        timestamp updated_at
    }

    PURCHASE_ITEMS {
        bigint id PK
        bigint purchase_id FK
        bigint product_id FK
        bigint vendor_id FK
        int quantity
        decimal unit_price "DECIMAL(10,2)"
        decimal subtotal "DECIMAL(10,2)"
    }

    PRODUCT_REVIEWS {
        bigint id PK
        bigint product_id FK
        bigint customer_id FK
        int rating "1 to 5"
        text comment
        timestamp created_at
    }
```

## Relational Constraints & Integrity
1. **Primary Keys**: Every entity uses synthetic auto-incrementing `BIGINT` primary keys (`id`).
2. **Foreign Keys**: Enforces parent-child relationships (`ON DELETE RESTRICT` semantics to protect historical order data).
3. **Unique Constraints**:
   - `users.email`: Unique email per user account.
   - `purchases.order_number`: Unique business tracking code for orders.
   - `shopping_cart(customer_id, product_id)`: Prevents duplicate rows for the same product in a customer's cart.
   - `product_reviews(product_id, customer_id)`: Server-enforced unique constraint preventing duplicate customer reviews per product.
4. **Monetary Values**: All price, subtotal, and total fields use `DECIMAL(10,2)` to prevent floating point rounding inaccuracies.
