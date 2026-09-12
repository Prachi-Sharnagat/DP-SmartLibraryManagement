# 📚 Smart Library Management System (Full-Stack Java + React)

A comprehensive, production-grade Library Management System designed with clean object-oriented architecture, **5 GoF Design Patterns**, SQLite database persistence, an embedded **Java REST API Server**, and a modern **React + Vite Web Dashboard**.

---

## 🏗️ Architecture & GoF Design Patterns

```
[ React + Vite Web Dashboard (Port 5173) ]
                    │
                    ▼  HTTP / JSON (with CORS & RBAC Headers)
[ Java Embedded REST API Server (Port 8080 - HttpServer) ]
                    │
    ┌───────────────┼───────────────┬────────────────┬───────────────┐
    ▼               ▼               ▼                ▼               ▼
[Factory]      [Chain of]      [Observer]         [Proxy]        [Bridge]
ResourceFactory Responsibility  ReservationManager LibraryProxy  Notification
Physical/EBook/ Validation      Waitlist Event    RBAC / Audit   Email / SMS /
Journal Creator 5-Rule Pipeline Broadcasts        & Telemetry    In-App Channels
    │               │               │                │               │
    └───────────────┴───────────────┼────────────────┴───────────────┘
                                    ▼
                     [ SQLite Database (data/library.db) ]
```

---

## 🎯 Design Patterns Breakdown

| Design Pattern | Package | Implementation Details |
|---|---|---|
| **Factory Method** | `factory/` | Encapsulates the instantiation of polymorphic media: Physical Books (`Book`), DRM-protected digital items (`EBook`), and Academic Publications (`Journal`). |
| **Chain of Responsibility** | `chain/` | Sequential 5-rule loan validation pipeline: `MembershipHandler` ➔ `FineHandler` ($10 limit) ➔ `BorrowLimitHandler` (5 loan max) ➔ `AvailabilityHandler` ➔ `ReservationHandler`. |
| **Observer** | `observer/` | Automatic waitlist notifications: when a checked-out book is returned, `ReservationManager` automatically broadcasts event alerts to all subscribed `Member` instances. |
| **Proxy** | `proxy/` | Intercepts service calls to provide **Role-Based Access Control (RBAC)**, persistent **audit logging with timestamps**, and **latency telemetry profiling** before delegating to `RealLibraryService`. |
| **Bridge** | `bridge/` | Decouples notification abstractions (`BookAvailableNotification`) from delivery channel implementations (`EmailChannel`, `SMSChannel`, `InAppChannel`). |

---

## 🚀 How to Run

### 1. Run Automated Design Pattern Test Suite (11 Tests)
```powershell
npm run java:test
```
*(Runs comprehensive unit tests for Factory, Chain, Observer, Proxy, Bridge, and REST serialization with 100% pass rate)*

### 2. Start Java REST API Server (Port 8080)
```powershell
npm run java:server
```
*(Runs embedded HTTP API on `http://localhost:8080/api` with full CORS support and SQLite persistence)*

### 3. Start React Web Dashboard (Port 5173)
```powershell
npm run dev
```
*(Launches the modern web UI. The header will automatically show `Java API: 8080 Online`)*

### 4. Interactive Java Console CLI
```powershell
npm run java:cli
```

### 5. Automated Pattern Verification Showcase Demo
```powershell
npm run java:demo
```

---

## 🌟 Polish & Special Features

1. **📷 AI Optical Barcode & QR Code Scanner**:
   - Integrated camera viewfinder simulation in Issue and Return workflows with instant identification of member IDs and book ISBNs.
2. **💳 Fine Payment & Instant Settlement Modal**:
   - Interactive payment gateway simulation (Credit Card, Cash at Desk, Digital Wallet) that clears member balances and writes SQLite audit records.
3. **📊 PDF & CSV Export Suite**:
   - Instant export of Master Catalog CSV, Circulation Ledger CSV, Member Roster CSV, and printable/PDF formatted operations reports.
4. **🔍 Multi-Attribute Advanced Search**:
   - Instant filtering by media type (Books, E-Books, Journals), availability status, author, and shelf location.
5. **🛡️ Proxy Audit Log & Telemetry Visualizer**:
   - Live inspection of RBAC interceptor logs, caller permissions, execution latency metrics, and security audit records.
