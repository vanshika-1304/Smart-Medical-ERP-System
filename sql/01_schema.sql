-- ============================================================
-- Smart Medical ERP System — Oracle SQL Schema v2.0
-- All tables normalized to 3NF
-- ============================================================

-- 1. USERS
CREATE TABLE USERS (
    user_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username        VARCHAR2(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR2(256) NOT NULL,
    role            VARCHAR2(20)  NOT NULL CHECK (role IN ('ADMIN','STAFF','SALESMAN','OWNER')),
    is_active       CHAR(1)       DEFAULT 'Y' CHECK (is_active IN ('Y','N')),
    last_login      TIMESTAMP,
    created_at      TIMESTAMP     DEFAULT SYSTIMESTAMP,
    created_by      NUMBER
);

-- 2. MEDICINES
CREATE TABLE MEDICINES (
    medicine_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR2(100) NOT NULL,
    category        VARCHAR2(50),
    hsn_code        VARCHAR2(20),
    composition     VARCHAR2(200),
    company         VARCHAR2(100),
    gst_pct         NUMBER(5,2)   DEFAULT 0,
    rack_location   VARCHAR2(50),
    is_active       CHAR(1)       DEFAULT 'Y',
    created_at      TIMESTAMP     DEFAULT SYSTIMESTAMP,
    created_by      NUMBER REFERENCES USERS(user_id)
);

-- 3. SALESMEN
CREATE TABLE SALESMEN (
    salesman_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR2(100) NOT NULL,
    phone           VARCHAR2(15),
    commission_pct  NUMBER(5,2)   DEFAULT 0,
    is_active       CHAR(1)       DEFAULT 'Y',
    created_at      TIMESTAMP     DEFAULT SYSTIMESTAMP,
    created_by      NUMBER REFERENCES USERS(user_id)
);

-- 4. ROUTES
CREATE TABLE ROUTES (
    route_id        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    route_name      VARCHAR2(100) NOT NULL,
    area            VARCHAR2(100),
    salesman_id     NUMBER REFERENCES SALESMEN(salesman_id),
    is_active       CHAR(1)       DEFAULT 'Y',
    created_at      TIMESTAMP     DEFAULT SYSTIMESTAMP,
    created_by      NUMBER REFERENCES USERS(user_id)
);

-- 5. CUSTOMERS
CREATE TABLE CUSTOMERS (
    customer_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    shop_name           VARCHAR2(100) NOT NULL,
    owner_name          VARCHAR2(100),
    phone               VARCHAR2(15),
    gstin               VARCHAR2(20),
    address             VARCHAR2(200),
    route_id            NUMBER REFERENCES ROUTES(route_id),
    credit_limit        NUMBER(12,2)  DEFAULT 0,
    outstanding_balance NUMBER(12,2)  DEFAULT 0,
    is_active           CHAR(1)       DEFAULT 'Y',
    created_at          TIMESTAMP     DEFAULT SYSTIMESTAMP,
    created_by          NUMBER REFERENCES USERS(user_id)
);

-- 6. SUPPLIERS
CREATE TABLE SUPPLIERS (
    supplier_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                VARCHAR2(100) NOT NULL,
    contact_no          VARCHAR2(15),
    gstin               VARCHAR2(20),
    address             VARCHAR2(200),
    credit_limit        NUMBER(12,2)  DEFAULT 0,
    outstanding_balance NUMBER(12,2)  DEFAULT 0,
    is_active           CHAR(1)       DEFAULT 'Y',
    created_at          TIMESTAMP     DEFAULT SYSTIMESTAMP,
    created_by          NUMBER REFERENCES USERS(user_id)
);

-- 7. MEDICINE_BATCHES
CREATE TABLE MEDICINE_BATCHES (
    batch_id        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    medicine_id     NUMBER NOT NULL REFERENCES MEDICINES(medicine_id),
    batch_no        VARCHAR2(50) NOT NULL,
    expiry_date     DATE NOT NULL,
    mrp             NUMBER(10,2) NOT NULL,
    purchase_rate   NUMBER(10,2) NOT NULL,
    stock_qty       NUMBER       DEFAULT 0,
    min_stock_alert NUMBER       DEFAULT 10,
    is_active       CHAR(1)      DEFAULT 'Y',
    created_at      TIMESTAMP    DEFAULT SYSTIMESTAMP,
    created_by      NUMBER REFERENCES USERS(user_id),
    UNIQUE (medicine_id, batch_no)
);

-- 8. PURCHASES
CREATE TABLE PURCHASES (
    purchase_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    supplier_id     NUMBER NOT NULL REFERENCES SUPPLIERS(supplier_id),
    invoice_no      VARCHAR2(50) NOT NULL,
    purchase_date   DATE         DEFAULT SYSDATE,
    total_amount    NUMBER(12,2) DEFAULT 0,
    payment_status  VARCHAR2(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING','PARTIAL','PAID')),
    created_at      TIMESTAMP    DEFAULT SYSTIMESTAMP,
    created_by      NUMBER REFERENCES USERS(user_id)
);

-- 9. PURCHASE_ITEMS
CREATE TABLE PURCHASE_ITEMS (
    item_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    purchase_id     NUMBER NOT NULL REFERENCES PURCHASES(purchase_id),
    medicine_id     NUMBER NOT NULL REFERENCES MEDICINES(medicine_id),
    batch_id        NUMBER NOT NULL REFERENCES MEDICINE_BATCHES(batch_id),
    qty             NUMBER       NOT NULL,
    rate            NUMBER(10,2) NOT NULL,
    mrp             NUMBER(10,2) NOT NULL,
    gst_pct         NUMBER(5,2)  DEFAULT 0,
    amount          NUMBER(12,2) NOT NULL
);

-- 10. SALES
CREATE TABLE SALES (
    sale_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id     NUMBER NOT NULL REFERENCES CUSTOMERS(customer_id),
    salesman_id     NUMBER REFERENCES SALESMEN(salesman_id),
    bill_no         VARCHAR2(50) NOT NULL UNIQUE,
    sale_date       DATE         DEFAULT SYSDATE,
    subtotal        NUMBER(12,2) DEFAULT 0,
    discount        NUMBER(10,2) DEFAULT 0,
    gst             NUMBER(10,2) DEFAULT 0,
    net_total       NUMBER(12,2) DEFAULT 0,
    payment_status  VARCHAR2(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING','PARTIAL','PAID')),
    created_at      TIMESTAMP    DEFAULT SYSTIMESTAMP,
    created_by      NUMBER REFERENCES USERS(user_id)
);

-- 11. SALE_ITEMS
CREATE TABLE SALE_ITEMS (
    item_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sale_id         NUMBER NOT NULL REFERENCES SALES(sale_id),
    medicine_id     NUMBER NOT NULL REFERENCES MEDICINES(medicine_id),
    batch_id        NUMBER NOT NULL REFERENCES MEDICINE_BATCHES(batch_id),
    qty_sold        NUMBER       NOT NULL,
    mrp             NUMBER(10,2) NOT NULL,
    purchase_rate   NUMBER(10,2) NOT NULL,
    discount        NUMBER(10,2) DEFAULT 0,
    gst_pct         NUMBER(5,2)  DEFAULT 0,
    amount          NUMBER(12,2) NOT NULL
);

-- 12. PAYMENTS (unified — customers + suppliers)
CREATE TABLE PAYMENTS (
    payment_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    party_type      CHAR(1)      NOT NULL CHECK (party_type IN ('C','S')),  -- C=Customer, S=Supplier
    party_id        NUMBER       NOT NULL,
    payment_mode    VARCHAR2(20) NOT NULL CHECK (payment_mode IN ('CASH','CHEQUE','UPI','NEFT','RTGS')),
    amount          NUMBER(12,2) NOT NULL,
    reference_no    VARCHAR2(50),
    payment_date    DATE         DEFAULT SYSDATE,
    sale_id         NUMBER REFERENCES SALES(sale_id),
    purchase_id     NUMBER REFERENCES PURCHASES(purchase_id),
    cheque_status   VARCHAR2(20) CHECK (cheque_status IN ('PENDING','CLEARED','BOUNCED')),
    created_at      TIMESTAMP    DEFAULT SYSTIMESTAMP,
    created_by      NUMBER REFERENCES USERS(user_id)
);

-- ============================================================
-- INDEXES for performance
-- ============================================================
CREATE INDEX idx_batches_medicine   ON MEDICINE_BATCHES(medicine_id);
CREATE INDEX idx_batches_expiry     ON MEDICINE_BATCHES(expiry_date);
CREATE INDEX idx_sales_customer     ON SALES(customer_id);
CREATE INDEX idx_sales_date         ON SALES(sale_date);
CREATE INDEX idx_sales_salesman     ON SALES(salesman_id);
CREATE INDEX idx_sale_items_sale    ON SALE_ITEMS(sale_id);
CREATE INDEX idx_purchase_supplier  ON PURCHASES(supplier_id);
CREATE INDEX idx_purch_items_purch  ON PURCHASE_ITEMS(purchase_id);
CREATE INDEX idx_payments_party     ON PAYMENTS(party_type, party_id);
CREATE INDEX idx_customers_route    ON CUSTOMERS(route_id);

-- ============================================================
-- SEQUENCE for Bill Number
-- ============================================================
CREATE SEQUENCE bill_no_seq START WITH 1000 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- DEFAULT ADMIN USER (password: Admin@123 -> SHA-256)
-- ============================================================
INSERT INTO USERS (username, password_hash, role, is_active)
VALUES ('admin', 
        'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7', 
        'ADMIN', 'Y');
COMMIT;
