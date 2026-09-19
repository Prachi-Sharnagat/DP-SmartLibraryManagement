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

        // Seed Users
        User u1 = new User("U101", "Alice Johnson", "alice@example.com", "555-0101", true, 0.0, 0, "MEMBER"); u1.setPasswordHash(defaultHash); saveUser(u1);
        User u2 = new User("U102", "Bob Smith", "bob@example.com", "555-0102", true, 15.50, 2, "MEMBER"); u2.setPasswordHash(defaultHash); saveUser(u2);
        User u3 = new User("U103", "Charlie Davis", "charlie@example.com", "555-0103", false, 0.0, 0, "MEMBER"); u3.setPasswordHash(defaultHash); saveUser(u3);
        User u4 = new User("U104", "Diana Prince", "diana@example.com", "555-0104", true, 0.0, 5, "MEMBER"); u4.setPasswordHash(defaultHash); saveUser(u4);
        User u5 = new User("U105", "Evan Wright", "evan.wright@stanford.edu", "555-0105", true, 0.0, 1, "FACULTY"); u5.setPasswordHash(defaultHash); saveUser(u5);
        User u999 = new User("U999", "Admin Officer", "admin@library.org", "555-0999", true, 0.0, 0, "ADMIN"); u999.setPasswordHash(defaultHash); saveUser(u999);

        // Seed Resources via Factory Method
        saveResource(factory.ResourceFactory.createBook("B001", "Design Patterns: Elements of Reusable Object-Oriented Software", "Gang of Four", "978-0201633610", "Shelf-CS-01"));
        saveResource(factory.ResourceFactory.createBook("B002", "Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin", "978-0132350884", "Shelf-CS-02"));
        
        LibraryResource b003 = factory.ResourceFactory.createBook("B003", "Introduction to Algorithms", "Thomas H. Cormen", "978-0262033848", "Shelf-CS-03");
        b003.setAvailable(false); // Currently out
        saveResource(b003);
        
        saveResource(factory.ResourceFactory.createBook("B004", "Artificial Intelligence: A Modern Approach", "Stuart Russell & Peter Norvig", "978-0136042594", "Shelf-AI-04"));

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
        resourceMap.put(resource.getId(), resource);
    }

    public LibraryResource getResource(String resourceId) {
        return resourceMap.get(resourceId);
    }

    public List<LibraryResource> getAllResources() {
        return new ArrayList<>(resourceMap.values());
    }

    public boolean deleteResource(String resourceId) {
        return resourceMap.remove(resourceId) != null;
    }

    // Loan Operations
    public void saveLoan(Loan loan) {
        loanMap.put(loan.getLoanId(), loan);
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
