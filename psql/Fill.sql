-- =====================================================================
-- ARCHIVO: Fill.sql
-- TODOS LOS ROLES CORREGIDOS PARA CUMPLIR CON LAS REGLAS DEL PROYECTO
-- =====================================================================

INSERT INTO customers (first_name, last_name, address, phone, email) VALUES
('Juan', 'Perez', 'Av. Central 101', '555-1001', 'juan.perez@email.com'),
('Maria', 'Lopez', 'Calle Norte 22', '555-1002', 'maria.lopez@email.com'),
('Carlos', 'Ramirez', 'Col. Moderna 33', '555-1003', 'carlos.ramirez@email.com'),
('Ana', 'Torres', 'Av. Reforma 44', '555-1004', 'ana.torres@email.com'),
('Luis', 'Martinez', 'Calle Sur 55', '555-1005', 'luis.martinez@email.com'),
('Sofia', 'Gomez', 'Centro 66', '555-1006', 'sofia.gomez@email.com'),
('Miguel', 'Hernandez', 'Av. Juarez 77', '555-1007', 'miguel.hernandez@email.com'),
('Laura', 'Diaz', 'Zona Rosa 88', '555-1008', 'laura.diaz@email.com'),
('Pedro', 'Vargas', 'Calle Luna 99', '555-1009', 'pedro.vargas@email.com'),
('Elena', 'Castro', 'Av. Sol 111', '555-1010', 'elena.castro@email.com');

-- =========================
-- staff (ROLES AJUSTADOS A TU REGLA FIXED:...)
-- =========================
INSERT INTO staff (first_name, last_name, role_staff, birth_date, address, phone, email) VALUES
('Roberto', 'Jimenez', 'Manager', '1985-03-15', 'Av. Uno 10', '555-2001', 'roberto.jimenez@email.com'),
('Patricia', 'Santos', 'Cashier', '1990-06-20', 'Calle Dos 20', '555-2002', 'patricia.santos@email.com'),
('Fernando', 'Ruiz', 'Sales Associate', '1992-01-10', 'Calle Tres 30', '555-2003', 'fernando.ruiz@email.com'),
('Lucia', 'Morales', 'Inventory Manager', '1988-11-05', 'Av. Cuatro 40', '555-2004', 'lucia.morales@email.com'),
('Jorge', 'Navarro', 'Assistant Manager', '1995-07-12', 'Calle Cinco 50', '555-2005', 'jorge.navarro@email.com'),
('Diana', 'Flores', 'Cashier', '1993-09-17', 'Av. Seis 60', '555-2006', 'diana.flores@email.com'),
('Ricardo', 'Mendoza', 'Sales Associate', '1987-12-01', 'Calle Siete 70', '555-2007', 'ricardo.mendoza@email.com'),
('Monica', 'Rojas', 'Manager', '1991-08-08', 'Av. Ocho 80', '555-2008', 'monica.rojas@email.com'),
('Daniel', 'Silva', 'Inventory Manager', '1984-05-22', 'Calle Nueve 90', '555-2009', 'daniel.silva@email.com'),
('Gabriela', 'Ortega', 'Assistant Manager', '1996-04-14', 'Av. Diez 100', '555-2010', 'gabriela.ortega@email.com');

-- =========================
-- sales_outlets
-- =========================
INSERT INTO sales_outlets (name, address, phone) VALUES
('Outlet Centro', 'Centro 101', '555-3001'),
('Outlet Norte', 'Norte 202', '555-3002'),
('Outlet Sur', 'Sur 303', '555-3003'),
('Outlet Este', 'Este 404', '555-3004'),
('Outlet Oeste', 'Oeste 505', '555-3005'),
('Outlet Plaza', 'Plaza 606', '555-3006'),
('Outlet Mall', 'Mall 707', '555-3007'),
('Outlet Express', 'Express 808', '555-3008'),
('Outlet Premium', 'Premium 909', '555-3009'),
('Outlet Mega', 'Mega 1001', '555-3010');

-- =========================
-- payment_methods
-- =========================
INSERT INTO payment_methods (payment_method_code, payment_method_name, payment_method_description) VALUES
('CASH', 'Cash', 'Cash payment'),
('CARD', 'Credit Card', 'Card payment'),
('DEBIT', 'Debit Card', 'Debit payment'),
('TRANSFER', 'Bank Transfer', 'Bank transfer'),
('PAYPAL', 'PayPal', 'Online payment'),
('APPLEPAY', 'Apple Pay', 'Mobile payment'),
('GOOGLEPAY', 'Google Pay', 'Mobile payment'),
('CRYPTO', 'Cryptocurrency', 'Crypto payment'),
('CHECK', 'Check', 'Bank check'),
('STORECREDIT', 'Store Credit', 'Store balance');

-- =========================
-- products
-- =========================
INSERT INTO products (name, description, product_code, stock, wholesale_price, retail_price) VALUES
('Laptop', 'Gaming laptop', 'PROD001', 50, 700.00, 950.00),
('Mouse', 'Wireless mouse', 'PROD002', 100, 10.00, 20.00),
('Keyboard', 'Mechanical keyboard', 'PROD003', 80, 25.00, 45.00),
('Monitor', '24 inch monitor', 'PROD004', 60, 120.00, 180.00),
('Headphones', 'Noise cancelling', 'PROD005', 70, 35.00, 60.00),
('Printer', 'Inkjet printer', 'PROD006', 40, 80.00, 130.00),
('USB Cable', 'USB-C cable', 'PROD007', 200, 2.00, 8.00),
('Desk Chair', 'Ergonomic chair', 'PROD008', 30, 90.00, 150.00),
('Webcam', 'HD webcam', 'PROD009', 90, 20.00, 40.00),
('Tablet', '10 inch tablet', 'PROD010', 25, 150.00, 250.00);

-- =========================
-- sales_transactions
-- =========================
INSERT INTO sales_transactions
(transaction_datetime, wholesale_price, retail_price, customer_id, staff_id, sales_outlet_id)
VALUES
('2026-05-01 10:00:00', 0, 0, 1, 1, 1),
('2026-05-02 11:00:00', 0, 0, 2, 2, 2),
('2026-05-03 12:00:00', 0, 0, 3, 3, 3),
('2026-05-04 13:00:00', 0, 0, 4, 4, 4),
('2026-05-05 14:00:00', 0, 0, 5, 5, 5),
('2026-05-06 15:00:00', 0, 0, 6, 6, 1),
('2026-05-07 16:00:00', 0, 0, 7, 7, 2),
('2026-05-08 17:00:00', 0, 0, 8, 8, 3),
('2026-05-09 18:00:00', 0, 0, 9, 9, 4),
('2026-05-10 19:00:00', 0, 0, 10, 10, 5);

-- =========================
-- transaction_products
-- =========================
INSERT INTO transaction_products (transaction_id, product_id, quantity) VALUES
(1, 1, 1),
(1, 2, 2),
(2, 2, 1),
(2, 3, 1),
(3, 3, 1),
(3, 4, 1),
(4, 4, 1),
(4, 5, 1),
(5, 5, 2),
(5, 6, 1),
(6, 6, 1),
(6, 7, 5),
(7, 7, 10),
(7, 8, 1),
(8, 8, 1),
(8, 9, 2),
(9, 9, 1),
(9, 10, 1),
(10, 10, 1),
(10, 1, 1);

-- =========================
-- payments
-- =========================
INSERT INTO payments (amount, payment_date, payment_reference, transaction_id, payment_method_code) VALUES
(950.00, '2026-05-01', 'PAY001', 1, 'CARD'),
(40.00, '2026-05-02', 'PAY002', 2, 'CASH'),
(45.00, '2026-05-03', 'PAY003', 3, 'DEBIT'),
(180.00, '2026-05-04', 'PAY004', 4, 'TRANSFER'),
(60.00, '2026-05-05', 'PAY005', 5, 'PAYPAL'),
(130.00, '2026-05-06', 'PAY006', 6, 'APPLEPAY'),
(80.00, '2026-05-07', 'PAY007', 7, 'GOOGLEPAY'),
(150.00, '2026-05-08', 'PAY008', 8, 'CRYPTO'),
(40.00, '2026-05-09', 'PAY009', 9, 'CHECK'),
(250.00, '2026-05-10', 'PAY010', 10, 'STORECREDIT');
