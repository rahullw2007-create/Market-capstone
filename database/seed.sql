-- Seed Data for ZenithBazaar Multi-Vendor E-Commerce

INSERT INTO users (id, email, password_hash, full_name, role, is_active) VALUES
(1, 'admin@zenithbazaar.com', '$2a$10$rN8v6fA/.m7wK9A0r1Nqee5Y5L3E5.S6/8f4Z9.B2C0D1E2F3G4H5', 'System Administrator', 'ADMINISTRATOR', true),
(2, 'vendor1@techstore.com', '$2a$10$rN8v6fA/.m7wK9A0r1Nqee5Y5L3E5.S6/8f4Z9.B2C0D1E2F3G4H5', 'Apex Tech Solutions', 'VENDOR', true),
(3, 'vendor2@fashionhub.com', '$2a$10$rN8v6fA/.m7wK9A0r1Nqee5Y5L3E5.S6/8f4Z9.B2C0D1E2F3G4H5', 'Urban Vogue Outfitters', 'VENDOR', true),
(4, 'customer1@gmail.com', '$2a$10$rN8v6fA/.m7wK9A0r1Nqee5Y5L3E5.S6/8f4Z9.B2C0D1E2F3G4H5', 'Alice Smith', 'CUSTOMER', true),
(5, 'customer2@yahoo.com', '$2a$10$rN8v6fA/.m7wK9A0r1Nqee5Y5L3E5.S6/8f4Z9.B2C0D1E2F3G4H5', 'Bob Johnson', 'CUSTOMER', true),
(6, 'customer3@outlook.com', '$2a$10$rN8v6fA/.m7wK9A0r1Nqee5Y5L3E5.S6/8f4Z9.B2C0D1E2F3G4H5', 'Charlie Brown', 'CUSTOMER', true);

INSERT INTO products (id, vendor_id, name, description, price, quantity, category, image_url, is_active) VALUES
(1, 2, 'Pro Ultra Wireless Headphones', 'Active noise canceling over-ear Bluetooth headphones with 40h battery life.', 199.99, 25, 'Electronics', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500', true),
(2, 2, 'Ergonomic Mechanical Keyboard', 'RGB backlit mechanical keyboard with tactile brown switches and wrist rest.', 129.50, 15, 'Electronics', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500', true),
(3, 2, '4K Ultra HD Action Camera', 'Waterproof 4K action camera with image stabilization and dual screens.', 299.00, 10, 'Electronics', 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500', true),
(4, 2, 'Smart Fitness Watch', 'Heart rate monitor, GPS tracking, sleep analytics, and 7-day battery life.', 149.99, 30, 'Electronics', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500', true),
(5, 3, 'Classic Denim Jacket', 'Premium vintage washed cotton denim jacket with durable stitching.', 79.95, 40, 'Fashion', 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=500', true),
(6, 3, 'Leather Minimalist Wallet', 'Genuine full-grain leather slim bi-fold wallet with RFID protection.', 45.00, 50, 'Fashion', 'https://images.unsplash.com/photo-1627123424574-724758594e93?w=500', true),
(7, 3, 'Stainless Steel Water Bottle', 'Double-wall vacuum insulated 1L water bottle keeps drinks cold for 24h.', 24.99, 100, 'Home & Kitchen', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500', true),
(8, 3, 'Noise-Canceling Desk Lamp', 'Dimmable LED desk lamp with wireless smartphone charging pad and timer.', 59.90, 20, 'Home & Kitchen', 'https://images.unsplash.com/photo-1534073828943-f801091bb18c?w=500', true),
(9, 3, 'Breathable Trail Running Shoes', 'Lightweight cushioning running shoes with high-traction rubber outsole.', 89.99, 35, 'Fitness', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500', true);

INSERT INTO purchases (id, order_number, customer_id, total_amount, status, created_at) VALUES
(1, 'ORD-20260919-001', 4, 199.99, 'DELIVERED', CURRENT_TIMESTAMP);

INSERT INTO purchase_items (id, purchase_id, product_id, vendor_id, quantity, unit_price, subtotal) VALUES
(1, 1, 1, 2, 1, 199.99, 199.99);

INSERT INTO product_reviews (id, product_id, customer_id, rating, comment, created_at) VALUES
(1, 1, 4, 5, 'Exceptional sound quality and very comfortable for long work sessions!', CURRENT_TIMESTAMP);

-- Restart sequences past highest explicit seed IDs
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE products ALTER COLUMN id RESTART WITH 100;
ALTER TABLE purchases ALTER COLUMN id RESTART WITH 100;
ALTER TABLE purchase_items ALTER COLUMN id RESTART WITH 100;
ALTER TABLE product_reviews ALTER COLUMN id RESTART WITH 100;
