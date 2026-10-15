package database;

import factory.Book;
import factory.EBook;
import factory.Journal;
import factory.LibraryResource;
import model.AuditLogEntry;
import model.Loan;
import model.NotificationRecord;
import model.ReservationRecord;
import model.User;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Database manager supporting SQLite database connectivity with fallback in-memory persistence.
 */
public class Database {
    private static Database instance;
    private Connection connection;
    private boolean isSqliteActive = false;

    // In-memory fallback repositories
    private final Map<String, User> userMap = new ConcurrentHashMap<>();
    private final Map<String, LibraryResource> resourceMap = new ConcurrentHashMap<>();
    private final Map<String, Loan> loanMap = new ConcurrentHashMap<>();
    private final List<AuditLogEntry> auditLogs = new CopyOnWriteArrayList<>();
    private final List<NotificationRecord> notifications = new CopyOnWriteArrayList<>();
    private final Map<String, ReservationRecord> reservationMap = new ConcurrentHashMap<>();

    private Database() {
        initDatabase();
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dbDir = new File("data");
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:data/library.db");
            isSqliteActive = true;
            createTables();
            System.out.println("  [Database] Connected to SQLite database: data/library.db");
        } catch (Throwable t) {
            System.out.println("  [Database] SQLite JDBC driver not detected or DB error (" + t.getClass().getSimpleName() + "). Initializing resilient in-memory persistent store.");
            isSqliteActive = false;
        }

        seedInitialData();
    }

    private void createTables() {
        if (!isSqliteActive || connection == null) return;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "email TEXT, " +
                    "phone TEXT, " +
                    "membership_active INTEGER, " +
                    "outstanding_fine REAL, " +
                    "borrowed_count INTEGER, " +
                    "role TEXT, " +
                    "max_loans INTEGER, " +
                    "department TEXT, " +
                    "semester TEXT, " +
                    "joined_date TEXT, " +
                    "avatar TEXT, " +
                    "password_hash TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS resources (" +
                    "resource_id TEXT PRIMARY KEY, " +
                    "title TEXT, " +
                    "type TEXT, " +
                    "author_publisher TEXT, " +
                    "isbn_format TEXT, " +
                    "location_url TEXT, " +
                    "is_available INTEGER, " +
                    "extra_val REAL, " +
                    "category TEXT, " +
                    "year INTEGER, " +
                    "edition TEXT, " +
                    "total_copies INTEGER, " +
                    "available_copies INTEGER, " +
                    "borrow_days INTEGER, " +
                    "fine_per_day REAL, " +
                    "is_reference_only INTEGER, " +
                    "description TEXT, " +
                    "file_size_mb REAL, " +
                    "download_url TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS loans (" +
                    "loan_id TEXT PRIMARY KEY, " +
                    "user_id TEXT, " +
                    "resource_id TEXT, " +
                    "borrow_date TEXT, " +
                    "due_date TEXT, " +
                    "return_date TEXT, " +
                    "fine REAL, " +
                    "is_returned INTEGER, " +
                    "issued_by TEXT, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id), " +
                    "FOREIGN KEY(resource_id) REFERENCES resources(resource_id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                    "reservation_id TEXT PRIMARY KEY, " +
                    "resource_id TEXT, " +
                    "user_id TEXT, " +
                    "position INTEGER, " +
                    "status TEXT, " +
                    "request_date TEXT, " +
                    "ready_until TEXT, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id), " +
                    "FOREIGN KEY(resource_id) REFERENCES resources(resource_id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                    "notification_id TEXT PRIMARY KEY, " +
                    "user_id TEXT, " +
                    "type TEXT, " +
                    "title TEXT, " +
                    "message TEXT, " +
                    "timestamp TEXT, " +
                    "is_read INTEGER, " +
                    "category TEXT, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                    "log_id TEXT PRIMARY KEY, " +
                    "timestamp TEXT, " +
                    "date TEXT, " +
                    "user TEXT, " +
                    "action TEXT, " +
                    "target TEXT, " +
                    "status TEXT, " +
                    "latency TEXT, " +
                    "ip_address TEXT)");

        } catch (SQLException e) {
            System.err.println("  [Database Error] Table creation error: " + e.getMessage());
        }
    }

    private void seedInitialData() {
        // Default password hash for "password123"
        String defaultHash = service.AuthService.hashPassword("password123");

        // Seed Users via Builder Pattern (UserBuilder)
        saveUser(new builder.UserBuilder().userId("U101").name("Alice Johnson").email("alice@example.com").phone("555-0101").role("MEMBER").membershipActive(true).outstandingFine(0.0).currentBorrowedCount(0).passwordHash(defaultHash).build());
        saveUser(new builder.UserBuilder().userId("U102").name("Bob Smith").email("bob@example.com").phone("555-0102").role("MEMBER").membershipActive(true).outstandingFine(15.50).currentBorrowedCount(2).passwordHash(defaultHash).build());
        saveUser(new builder.UserBuilder().userId("U103").name("Charlie Davis").email("charlie@example.com").phone("555-0103").role("MEMBER").membershipActive(false).outstandingFine(0.0).currentBorrowedCount(0).passwordHash(defaultHash).build());
        saveUser(new builder.UserBuilder().userId("U104").name("Diana Prince").email("diana@example.com").phone("555-0104").role("MEMBER").membershipActive(true).outstandingFine(0.0).currentBorrowedCount(5).passwordHash(defaultHash).build());
        saveUser(new builder.UserBuilder().userId("U105").name("Evan Wright").email("evan.wright@stanford.edu").phone("555-0105").role("FACULTY").membershipActive(true).outstandingFine(0.0).currentBorrowedCount(1).department("Computer Science").passwordHash(defaultHash).build());
        saveUser(new builder.UserBuilder().userId("U106").name("Fiona Gallagher").email("fiona@student.edu").phone("555-0106").role("STUDENT").membershipActive(true).outstandingFine(0.0).currentBorrowedCount(1).department("Information Technology").semester("4th").passwordHash(defaultHash).build());
        saveUser(new builder.UserBuilder().userId("U999").name("Admin Officer").email("admin@library.org").phone("555-0999").role("ADMIN").membershipActive(true).outstandingFine(0.0).currentBorrowedCount(0).passwordHash(defaultHash).build());

        // Seed Resources via Builder Pattern (BookBuilder) & Factory Method
        saveResource(new builder.BookBuilder()
                .id("B001").title("Design Patterns: Elements of Reusable Object-Oriented Software")
                .author("Gang of Four").isbn("978-0201633610").shelfLocation("Shelf-CS-01")
                .publisher("Addison-Wesley").edition("1st Edition").category("Software Engineering")
                .publicationYear(1994).isAvailable(true).build());

        saveResource(new builder.BookBuilder()
                .id("B002").title("Clean Code: A Handbook of Agile Software Craftsmanship")
                .author("Robert C. Martin").isbn("978-0132350884").shelfLocation("Shelf-CS-02")
                .publisher("Prentice Hall").edition("1st Edition").category("Software Engineering")
                .publicationYear(2008).isAvailable(true).build());

        saveResource(new builder.BookBuilder()
                .id("B003").title("Introduction to Algorithms")
                .author("Thomas H. Cormen").isbn("978-0262033848").shelfLocation("Shelf-CS-03")
                .publisher("MIT Press").edition("3rd Edition").category("Algorithms")
                .publicationYear(2009).isAvailable(false).build());

        saveResource(new builder.BookBuilder()
                .id("B004").title("Artificial Intelligence: A Modern Approach")
                .author("Stuart Russell & Peter Norvig").isbn("978-0136042594").shelfLocation("Shelf-AI-04")
                .publisher("Pearson").edition("4th Edition").category("Artificial Intelligence")
                .publicationYear(2020).isAvailable(true).build());

        saveResource(factory.ResourceFactory.createEBook("EB001", "Effective Java (3rd Edition)", "Joshua Bloch", 14.2, "PDF"));
        saveResource(factory.ResourceFactory.createEBook("EB002", "Architecting Enterprise Systems in Java", "Martin Fowler", 8.5, "EPUB"));
        saveResource(factory.ResourceFactory.createEBook("EB003", "Building Microservices: Designing Fine-Grained Systems", "Sam Newman", 11.8, "PDF"));

        saveResource(factory.ResourceFactory.createJournal("J001", "IEEE Software Architecture Quarterly", "IEEE Computer Society", 41, 3, false));
        saveResource(factory.ResourceFactory.createJournal("J002", "ACM Computing Surveys (Reference Archive)", "ACM", 55, 1, true)); // Reference Only

        // Seed Initial Active Loan for B003
        Loan l1 = new Loan("LN-INIT-001", "U102", "B003", LocalDate.now().minusDays(18), LocalDate.now().minusDays(4));
        l1.setFine(2.00);
        saveLoan(l1);

        // Seed Initial Audit Logs
        addAuditLog(new AuditLogEntry("AUD-001", "U999", "SYSTEM_INIT", "Database and Design Pattern services initialized successfully", true, 12));
        addAuditLog(new AuditLogEntry("AUD-002", "U102", "BORROW_COMPLETED", "TargetUser: U102, Resource: B003 | Success: true", true, 45));

        // Seed Initial Notifications
        addNotification(new NotificationRecord("NOTIF-001", "U101", "Alice Johnson", "IN_APP", "Welcome to Smart Library", "Your membership is active and in good standing.", "DELIVERED"));
    }

    // User Operations
    public void saveUser(User user) {
        userMap.put(user.getUserId(), user);
        if (isSqliteActive && connection != null) {
            String sql = "INSERT OR REPLACE INTO users (user_id, name, email, phone, membership_active, outstanding_fine, borrowed_count, role, max_loans, department, semester, joined_date, avatar, password_hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, user.getUserId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhone());
                ps.setInt(5, user.isMembershipActive() ? 1 : 0);
                ps.setDouble(6, user.getOutstandingFine());
                ps.setInt(7, user.getCurrentBorrowedCount());
                ps.setString(8, user.getRole());
                ps.setInt(9, user.getMaxLoans());
                ps.setString(10, user.getDepartment());
                ps.setString(11, user.getSemester());
                ps.setString(12, user.getJoinedDate());
                ps.setString(13, user.getAvatar());
                ps.setString(14, user.getPasswordHash());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("  [Database Error] saveUser error: " + e.getMessage());
            }
        }
    }

    public User getUser(String userId) {
        if (!isSqliteActive || connection == null) return userMap.get(userId);
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("membership_active") == 1,
                        rs.getDouble("outstanding_fine"),
                        rs.getInt("borrowed_count"),
                        rs.getString("role")
                );
                u.setMaxLoans(rs.getInt("max_loans"));
                u.setDepartment(rs.getString("department"));
                u.setSemester(rs.getString("semester"));
                u.setJoinedDate(rs.getString("joined_date"));
                u.setAvatar(rs.getString("avatar"));
                u.setPasswordHash(rs.getString("password_hash"));
                userMap.put(u.getUserId(), u); // cache update
                return u;
            }
        } catch (SQLException e) {
            System.err.println("  [Database Error] getUser error: " + e.getMessage());
        }
        return userMap.get(userId);
    }

    public User getUserByEmailOrId(String emailOrId) {
        if (emailOrId == null) return null;
        if (!isSqliteActive || connection == null) {
            for (User u : userMap.values()) {
                if (emailOrId.equalsIgnoreCase(u.getEmail()) || emailOrId.equalsIgnoreCase(u.getUserId())) {
                    return u;
                }
            }
            return null;
        }

        String sql = "SELECT * FROM users WHERE email = ? OR user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, emailOrId);
            ps.setString(2, emailOrId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("membership_active") == 1,
                        rs.getDouble("outstanding_fine"),
                        rs.getInt("borrowed_count"),
                        rs.getString("role")
                );
                u.setMaxLoans(rs.getInt("max_loans"));
                u.setDepartment(rs.getString("department"));
                u.setSemester(rs.getString("semester"));
                u.setJoinedDate(rs.getString("joined_date"));
                u.setAvatar(rs.getString("avatar"));
                u.setPasswordHash(rs.getString("password_hash"));
                userMap.put(u.getUserId(), u); // cache update
                return u;
            }
        } catch (SQLException e) {
            System.err.println("  [Database Error] getUserByEmailOrId error: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        if (!isSqliteActive || connection == null) return new ArrayList<>(userMap.values());
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("membership_active") == 1,
                        rs.getDouble("outstanding_fine"),
                        rs.getInt("borrowed_count"),
                        rs.getString("role")
                );
                u.setMaxLoans(rs.getInt("max_loans"));
                u.setDepartment(rs.getString("department"));
                u.setSemester(rs.getString("semester"));
                u.setJoinedDate(rs.getString("joined_date"));
                u.setAvatar(rs.getString("avatar"));
                list.add(u);
                userMap.put(u.getUserId(), u);
            }
            return list;
        } catch (SQLException e) {
            System.err.println("  [Database Error] getAllUsers error: " + e.getMessage());
        }
        return new ArrayList<>(userMap.values());
    }

    public boolean deleteUser(String userId) {
        userMap.remove(userId);
        if (isSqliteActive && connection != null) {
            String sql = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, userId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("  [Database Error] deleteUser error: " + e.getMessage());
            }
        }
        return true;
    }

    public boolean payFine(String userId, double amount) {
        User u = userMap.get(userId);
        if (u == null) return false;
        double currentFine = u.getOutstandingFine();
        double newFine = Math.max(0.0, currentFine - amount);
        u.setOutstandingFine(newFine);
        saveUser(u);
        return true;
    }

    // Resource Operations
    public void saveResource(LibraryResource resource) {
        if (resource == null) return;
        resourceMap.put(resource.getId(), resource);

        if (isSqliteActive && connection != null) {
            String sql = "INSERT OR REPLACE INTO resources (resource_id, title, type, author_publisher, isbn_format, location_url, is_available, extra_val, category, year, edition, total_copies, available_copies, borrow_days, fine_per_day, is_reference_only, description, file_size_mb, download_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, resource.getId());
                ps.setString(2, resource.getTitle());
                ps.setString(3, resource.getResourceType());

                if (resource instanceof Book) {
                    Book b = (Book) resource;
                    ps.setString(4, b.getAuthor());
                    ps.setString(5, b.getIsbn());
                    ps.setString(6, b.getShelfLocation());
                    ps.setInt(7, b.isAvailable() ? 1 : 0);
                    ps.setDouble(8, 0.0);
                    ps.setString(9, b.getCategory());
                    ps.setInt(10, b.getPublicationYear());
                    ps.setString(11, b.getEdition());
                    ps.setInt(12, b.getTotalCopies());
                    ps.setInt(13, b.getAvailableCopies());
                    ps.setInt(14, b.getMaxBorrowDays());
                    ps.setDouble(15, b.getLateFeePerDay());
                    ps.setInt(16, 0);
                    ps.setString(17, b.getPublisher());
                    ps.setDouble(18, 0.0);
                    ps.setString(19, "");
                } else if (resource instanceof EBook) {
                    EBook eb = (EBook) resource;
                    ps.setString(4, eb.getAuthor());
                    ps.setString(5, eb.getDownloadFormat());
                    ps.setString(6, "Digital Repository");
                    ps.setInt(7, eb.isAvailable() ? 1 : 0);
                    ps.setDouble(8, eb.getFileSizeMB());
                    ps.setString(9, "Digital");
                    ps.setInt(10, 2024);
                    ps.setString(11, "Digital Edition");
                    ps.setInt(12, 999);
                    ps.setInt(13, 999);
                    ps.setInt(14, eb.getMaxBorrowDays());
                    ps.setDouble(15, eb.getLateFeePerDay());
                    ps.setInt(16, 0);
                    ps.setString(17, "Digital Press");
                    ps.setDouble(18, eb.getFileSizeMB());
                    ps.setString(19, "http://library.internal/download/" + eb.getId());
                } else if (resource instanceof Journal) {
                    Journal j = (Journal) resource;
                    ps.setString(4, j.getPublisher());
                    ps.setString(5, "Vol." + j.getVolumeNumber() + " No." + j.getIssueNumber());
                    ps.setString(6, "Periodicals Wing");
                    ps.setInt(7, j.isAvailable() ? 1 : 0);
                    ps.setDouble(8, 0.0);
                    ps.setString(9, "Periodical");
                    ps.setInt(10, 2024);
                    ps.setString(11, "Issue " + j.getIssueNumber());
                    ps.setInt(12, 1);
                    ps.setInt(13, j.isAvailable() ? 1 : 0);
                    ps.setInt(14, j.getMaxBorrowDays());
                    ps.setDouble(15, j.getLateFeePerDay());
                    ps.setInt(16, j.isReferenceOnly() ? 1 : 0);
                    ps.setString(17, j.getPublisher());
                    ps.setDouble(18, 0.0);
                    ps.setString(19, "");
                } else {
                    ps.setString(4, "Unknown");
                    ps.setString(5, "N/A");
                    ps.setString(6, "Main Stacks");
                    ps.setInt(7, resource.isAvailable() ? 1 : 0);
                    ps.setDouble(8, 0.0);
                    ps.setString(9, "General");
                    ps.setInt(10, 2024);
                    ps.setString(11, "Standard");
                    ps.setInt(12, 1);
                    ps.setInt(13, resource.isAvailable() ? 1 : 0);
                    ps.setInt(14, resource.getMaxBorrowDays());
                    ps.setDouble(15, resource.getLateFeePerDay());
                    ps.setInt(16, 0);
                    ps.setString(17, "");
                    ps.setDouble(18, 0.0);
                    ps.setString(19, "");
                }
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("  [Database Error] saveResource error: " + e.getMessage());
            }
        }
    }

    public LibraryResource getResource(String resourceId) {
        return resourceMap.get(resourceId);
    }

    public List<LibraryResource> getAllResources() {
        return new ArrayList<>(resourceMap.values());
    }

    public boolean deleteResource(String resourceId) {
        resourceMap.remove(resourceId);
        if (isSqliteActive && connection != null) {
            String sql = "DELETE FROM resources WHERE resource_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, resourceId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("  [Database Error] deleteResource error: " + e.getMessage());
            }
        }
        return true;
    }

    // Loan Operations
    public void saveLoan(Loan loan) {
        if (loan == null) return;
        loanMap.put(loan.getLoanId(), loan);

        if (isSqliteActive && connection != null) {
            String sql = "INSERT OR REPLACE INTO loans (loan_id, user_id, resource_id, borrow_date, due_date, return_date, fine, is_returned, issued_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, loan.getLoanId());
                ps.setString(2, loan.getUserId());
                ps.setString(3, loan.getResourceId());
                ps.setString(4, loan.getBorrowDate() != null ? loan.getBorrowDate().toString() : null);
                ps.setString(5, loan.getDueDate() != null ? loan.getDueDate().toString() : null);
                ps.setString(6, loan.getReturnDate() != null ? loan.getReturnDate().toString() : null);
                ps.setDouble(7, loan.getFine());
                ps.setInt(8, loan.isReturned() ? 1 : 0);
                ps.setString(9, "STAFF");
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("  [Database Error] saveLoan error: " + e.getMessage());
            }
        }
    }

    public Loan getLoan(String loanId) {
        return loanMap.get(loanId);
    }

    public List<Loan> getAllLoans() {
        return new ArrayList<>(loanMap.values());
    }

    public List<Loan> getActiveLoansByUser(String userId) {
        List<Loan> list = new ArrayList<>();
        for (Loan l : loanMap.values()) {
            if (l.getUserId().equalsIgnoreCase(userId) && !l.isReturned()) {
                list.add(l);
            }
        }
        return list;
    }

    public Loan getActiveLoanForResource(String resourceId) {
        for (Loan l : loanMap.values()) {
            if (l.getResourceId().equalsIgnoreCase(resourceId) && !l.isReturned()) {
                return l;
            }
        }
        return null;
    }

    // Audit Logs
    public void addAuditLog(AuditLogEntry entry) {
        auditLogs.add(0, entry); // keep newest first
        if (auditLogs.size() > 500) {
            auditLogs.remove(auditLogs.size() - 1);
        }
    }

    public List<AuditLogEntry> getAuditLogs() {
        return new ArrayList<>(auditLogs);
    }

    // Notifications
    public void addNotification(NotificationRecord notif) {
        notifications.add(0, notif);
        if (notifications.size() > 300) {
            notifications.remove(notifications.size() - 1);
        }
    }

    public List<NotificationRecord> getNotifications() {
        return new ArrayList<>(notifications);
    }

    // Reservations
    public void addReservation(ReservationRecord record) {
        reservationMap.put(record.getResourceId(), record);
    }

    public ReservationRecord getReservation(String resourceId) {
        return reservationMap.get(resourceId);
    }

    public List<ReservationRecord> getAllReservations() {
        return new ArrayList<>(reservationMap.values());
    }

    public void removeReservation(String resourceId) {
        reservationMap.remove(resourceId);
    }

    public boolean isSqliteActive() {
        return isSqliteActive;
    }
}
