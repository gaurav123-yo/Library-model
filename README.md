# Smart Library Management System (Java Swing + MongoDB + MVC)

This is a college-grade **Smart Library Management System** developed in Java using a **Swing-based GUI**, **MongoDB** as the database, and following the **Model-View-Controller (MVC)** architectural pattern. 

The application utilizes **FlatLaf Look and Feel** for a sleek, modern Light Mode layout with clean input cards, borderless tables, and visual indicators.

---

## 🌟 Key Features

1.  **Dual Role Authentication (Librarian & Student)**
    *   Unified login screen with SHA-256 password hashing.
    *   Auto-detects roles to load specific dashboards.
2.  **Book Registry Management (CRUD)**
    *   Add, update, search, and delete books.
    *   Optimized search (by Title, Author, Category, ISBN) powered by indexing.
    *   Tracks catalog quantity and real-time availability.
3.  **Student Registry (CRUD) & Sync**
    *   Add and update student profiles (including category preferences).
    *   Automatically creates student login accounts (matching `Student ID` as username and default password `student123`) on registration and cleans them up on profile deletion.
4.  **Lending & Return Engine**
    *   Tracks borrows, due dates, return timestamps, and states.
    *   Blocks double issues of the same book to a single student.
    *   Automatic return date validations.
5.  **Fine Ledger**
    *   Calculates late fees dynamically (₹10 per day overdue).
    *   Librarians can inspect outstanding balances per student and mark transactions as paid.
6.  **Smart Recommendation Engine**
    *   Students can like/favorite books (stored uniquely).
    *   **Trending Books**: Displays catalog sorted by like aggregates.
    *   **Personalized Recommendations**: Combines explicit category preferences and historical borrowing categories to suggest unread high-popularity books first.
7.  **Auto Database Seeding & Graceful Failover**
    *   If MongoDB is offline locally, the app launches a GUI connection input dialog where users can paste connection parameters (e.g., MongoDB Atlas connection string).
    *   Automatically sets up indexes and seeds comprehensive mock data if the database is blank.

---

## 📂 Project Structure

```
smart-library-system/
├── pom.xml                  # Maven Configuration
├── config.properties        # MongoDB connection coordinates
├── run_with_maven.ps1       # Automated bootstrap script (downloads Maven, runs tests, launches GUI)
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── Main.java                        # Entry point
    │   │   ├── database/                        # Database interactions & DAOs
    │   │   │   ├── MongoDBConnection.java       # Client wrapper & collection seeding
    │   │   │   ├── UserDAO.java
    │   │   │   ├── BookDAO.java
    │   │   │   ├── StudentDAO.java
    │   │   │   ├── IssueRecordDAO.java
    │   │   │   ├── FineDAO.java
    │   │   │   └── BookLikeDAO.java
    │   │   ├── models/                          # Domain Entities
    │   │   ├── controllers/                     # Controllers (View/Model coordinators)
    │   │   └── views/                           # Swing panels & frames
    │   └── resources/
    └── test/
        └── java/
            └── LibrarySystemTest.java           # JUnit 5 Unit Tests
```

---

## 🚀 Getting Started

### Prerequisites
*   **Java JDK 17 or higher** must be installed and added to your system's Environment Variables (`java` command works from CLI).
*   **MongoDB** (Optional local instance running on `localhost:27017` OR a MongoDB Atlas connection string).

### Run Instructions (Windows PowerShell)
We have provided an automated script `run_with_maven.ps1` that will handle downloading Maven on-demand, executing tests, compiling the project, and launching the Swing window.

1.  Open **PowerShell** and navigate to the project directory:
    ```powershell
    cd "c:\Users\gaura\Desktop\Smart library model"
    ```
2.  Execute the bootstrapper:
    ```powershell
    .\run_with_maven.ps1
    ```
    *If you get an execution policy warning, run:*
    ```powershell
    Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
    .\run_with_maven.ps1
    ```

---

## 🔑 Test Credentials (Sample Data)

The database will be automatically seeded on first launch with the following default accounts:

### 1. Librarian / Administrator Login
*   **Username**: `admin`
*   **Password**: `admin123`

### 2. Student Accounts
*   **Student 1**:
    *   **Username**: `STU001`
    *   **Password**: `student123`
    *   *Features*: Has 1 active borrow record, 1 liked book, and customized category preferences.
*   **Student 2**:
    *   **Username**: `STU002`
    *   **Password**: `student123`
    *   *Features*: Has 1 outstanding unpaid fine of ₹30 from a late return.
