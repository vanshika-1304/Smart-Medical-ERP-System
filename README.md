# Smart Medical ERP System v2.0
**Java Full Stack Web Application for Medical Agency Management**

---

## Tech Stack
| Layer | Technology |
|-------|-----------|
| Frontend | HTML5, CSS3, JavaScript (ES6+), JSP |
| Backend | Java 17 + Servlets + JDBC |
| Database | Oracle SQL (3NF Normalized) |
| Server | Apache Tomcat 10 |
| Build | Maven |
| PDF | iText 7 |
| Excel | Apache POI |

---

## Project Structure
```
SmartMedicalERP/
├── pom.xml
├── sql/
│   └── 01_schema.sql              ← Run this first in Oracle
└── src/main/
    ├── java/com/smartmedical/
    │   ├── dao/                   ← Database Access Objects
    │   │   ├── UserDAO.java
    │   │   ├── MedicineDAO.java
    │   │   ├── CustomerDAO.java
    │   │   ├── SupplierDAO.java
    │   │   ├── SaleDAO.java       ← ACID sale transaction
    │   │   ├── PurchaseDAO.java   ← ACID purchase transaction
    │   │   ├── PaymentDAO.java    ← Receipt & payment engine
    │   │   ├── SalesmanDAO.java
    │   │   └── ReportDAO.java     ← All 10 MIS reports
    │   ├── model/                 ← POJO classes
    │   │   ├── User.java
    │   │   ├── Medicine.java
    │   │   ├── MedicineBatch.java ← FEFO alert logic
    │   │   ├── Customer.java
    │   │   ├── Supplier.java
    │   │   └── Transactions.java  ← Sale, SaleItem, Purchase, Payment
    │   ├── servlet/               ← Controller layer
    │   │   ├── DashboardServlet.java
    │   │   ├── auth/
    │   │   │   ├── LoginServlet.java
    │   │   │   ├── LogoutServlet.java
    │   │   │   └── AuthFilter.java
    │   │   ├── billing/BillingServlet.java
    │   │   ├── inventory/InventoryServlet.java
    │   │   ├── customer/CustomerServlet.java
    │   │   ├── supplier/SupplierServlet.java
    │   │   ├── payment/PaymentServlet.java
    │   │   └── reports/ReportServlet.java
    │   └── util/
    │       ├── DBConnection.java  ← Oracle UCP Pool (5–20 conns)
    │       ├── PasswordUtil.java  ← SHA-256 hashing
    │       ├── SessionUtil.java   ← Role-based session helper
    │       └── JsonUtil.java      ← Gson wrapper for AJAX
    └── webapp/
        ├── index.jsp
        ├── WEB-INF/web.xml
        └── jsp/
            ├── auth/login.jsp
            ├── dashboard.jsp
            └── (billing, inventory, customer, supplier, payment, reports — add JSPs here)
```

---

## Setup Instructions

### Step 1: Oracle Database
```sql
-- Connect as SYSDBA and create schema user
CREATE USER smartmedical IDENTIFIED BY "SmartMed@123";
GRANT CONNECT, RESOURCE TO smartmedical;
GRANT CREATE SEQUENCE TO smartmedical;

-- Then connect as smartmedical and run:
@sql/01_schema.sql
```

### Step 2: Configure DB Connection
Edit `DBConnection.java`:
```java
private static final String DB_URL      = "jdbc:oracle:thin:@localhost:1521:XE";
private static final String DB_USER     = "smartmedical";
private static final String DB_PASSWORD = "SmartMed@123";
```

### Step 3: Build with Maven
```bash
mvn clean package
```

### Step 4: Deploy to Tomcat 10
```bash
cp target/SmartMedicalERP.war $TOMCAT_HOME/webapps/
```

### Step 5: Access
```
http://localhost:8080/SmartMedicalERP/login
Username: admin
Password: Admin@123
```

---

## API Endpoints (All Servlets)

| Module | URL Pattern | Methods |
|--------|-------------|---------|
| Auth | `/login`, `/logout` | GET, POST |
| Dashboard | `/dashboard` | GET |
| Billing | `/billing/*` | GET, POST |
| Inventory | `/inventory/*` | GET, POST |
| Customers | `/customers/*` | GET, POST |
| Suppliers | `/suppliers/*` | GET, POST |
| Payments | `/payments/*` | GET, POST |
| Reports | `/reports/*` | GET |

### Key Billing Endpoints
```
POST /billing/create          → Create new sale bill (JSON)
POST /billing/return          → Process sale return
GET  /billing/search-medicine → AJAX medicine search ?q=
GET  /billing/batches         → FEFO batches ?medicineId=
```

### Key Reports
```
GET /reports/daily    ?date=YYYY-MM-DD
GET /reports/monthly  ?month=&year=
GET /reports/profit   ?from=&to=
GET /reports/gst      ?month=&year=
GET /reports/salesman ?from=&to=
```

---

## Security Features (BRD NFR)
- SHA-256 password hashing (no plain text ever stored)
- PreparedStatement everywhere (SQL injection prevention)
- Session-based auth with 30-min timeout
- Role-based access: ADMIN / STAFF / SALESMAN / OWNER
- AuthFilter intercepts all protected URLs

## ACID Transactions
- `SaleDAO.createSale()` — bill insert + stock deduction + outstanding update in one transaction
- `PurchaseDAO.createPurchase()` — purchase insert + batch creation + stock increase + supplier outstanding
- `PaymentDAO.recordCustomerReceipt()` — payment insert + outstanding reduction + bill status update

---

*Smart Medical ERP System v2.0 | BRD April 2026*
