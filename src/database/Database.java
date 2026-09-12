package database;

import factory.Book;
import factory.EBook;
import factory.Journal;
import factory.LibraryResource;
import model.Loan;
import model.User;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
                    "role TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS resources (" +
                    "resource_id TEXT PRIMARY KEY, " +
                    "title TEXT, " +
                    "type TEXT, " +
                    "author_publisher TEXT, " +
                    "isbn_format TEXT, " +
                    "location_url TEXT, " +
                    "is_available INTEGER, " +
                    "extra_val REAL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS loans (" +
                    "loan_id TEXT PRIMARY KEY, " +
                    "user_id TEXT, " +
                    "resource_id TEXT, " +
                    "borrow_date TEXT, " +
                    "due_date TEXT, " +
                    "return_date TEXT, " +
                    "fine REAL, " +
                    "is_returned INTEGER)");
        } catch (SQLException e) {
            System.err.println("  [Database Error] Table creation error: " + e.getMessage());
        }
    }

    private void seedInitialData() {
        // Seed Users
        saveUser(new User("U101", "Alice Johnson", "alice@example.com", "555-0101", true, 0.0, 0, "MEMBER"));
        saveUser(new User("U102", "Bob Smith", "bob@example.com", "555-0102", true, 15.50, 2, "MEMBER")); // High fine
        saveUser(new User("U103", "Charlie Davis", "charlie@example.com", "555-0103", false, 0.0, 0, "MEMBER")); // Inactive
        saveUser(new User("U104", "Diana Prince", "diana@example.com", "555-0104", true, 0.0, 5, "MEMBER")); // Max limit
        saveUser(new User("U999", "Admin Officer", "admin@library.org", "555-0999", true, 0.0, 0, "ADMIN"));

        // Seed Resources
        saveResource(new Book("B001", "Design Patterns: Elements of Reusable Object-Oriented Software", "Gang of Four", "978-0201633610", "Shelf-CS-01", true));
        saveResource(new Book("B002", "Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin", "978-0132350884", "Shelf-CS-02", true));
        saveResource(new Book("B003", "Introduction to Algorithms", "Thomas H. Cormen", "978-0262033848", "Shelf-CS-03", false)); // Currently out

        saveResource(new EBook("EB001", "Effective Java (3rd Edition)", "Joshua Bloch", 14.2, "PDF", true));
        saveResource(new EBook("EB002", "Architecting Enterprise Systems in Java", "Martin Fowler", 8.5, "EPUB", true));

        saveResource(new Journal("J001", "IEEE Software Architecture Quarterly", "IEEE Computer Society", 41, 3, false, true));
        saveResource(new Journal("J002", "ACM Computing Surveys (Reference Archive)", "ACM", 55, 1, true, true)); // Reference Only
    }

    // User Operations
    public void saveUser(User user) {
        userMap.put(user.getUserId(), user);
        if (isSqliteActive && connection != null) {
            String sql = "INSERT OR REPLACE INTO users VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, user.getUserId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhone());
                ps.setInt(5, user.isMembershipActive() ? 1 : 0);
                ps.setDouble(6, user.getOutstandingFine());
                ps.setInt(7, user.getCurrentBorrowedCount());
                ps.setString(8, user.getRole());
                ps.executeUpdate();
            } catch (SQLException ignored) {}
        }
    }

    public User getUser(String userId) {
        return userMap.get(userId);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
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
}
