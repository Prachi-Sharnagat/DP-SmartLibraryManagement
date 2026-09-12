package test;

import bridge.*;
import chain.ValidationHandler;
import database.Database;
import factory.Book;
import factory.EBook;
import factory.Journal;
import factory.LibraryResource;
import factory.ResourceFactory;
import model.AuditLogEntry;
import model.BorrowResult;
import model.ReturnResult;
import model.User;
import observer.Member;
import observer.Observer;
import observer.ReservationManager;
import proxy.LibraryService;
import proxy.LibraryServiceProxy;
import server.JsonUtils;
import service.LibraryServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive Automated Test Suite verifying all 5 GoF Design Patterns and system logic.
 */
public class PatternTestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("          🧪 SMART LIBRARY MANAGEMENT SYSTEM - DESIGN PATTERNS TEST SUITE       ");
        System.out.println("================================================================================\n");

        runTest("1. Factory Method Pattern", PatternTestSuite::testFactoryPattern);
        runTest("2. Chain of Responsibility - Rule 1 (Membership)", PatternTestSuite::testChainRule1Membership);
        runTest("3. Chain of Responsibility - Rule 2 (Fine Limit)", PatternTestSuite::testChainRule2Fine);
        runTest("4. Chain of Responsibility - Rule 3 (Borrow Limit)", PatternTestSuite::testChainRule3BorrowLimit);
        runTest("5. Chain of Responsibility - Rule 4 (Availability)", PatternTestSuite::testChainRule4Availability);
        runTest("6. Chain of Responsibility - Rule 5 (Reservation)", PatternTestSuite::testChainRule5Reservation);
        runTest("7. Chain of Responsibility - Full Success Path", PatternTestSuite::testChainFullSuccess);
        runTest("8. Observer Pattern (Waitlist Event Broadcast)", PatternTestSuite::testObserverPattern);
        runTest("9. Proxy Pattern (RBAC & Audit Telemetry)", PatternTestSuite::testProxyPattern);
        runTest("10. Bridge Pattern (Multi-Channel Notifications)", PatternTestSuite::testBridgePattern);
        runTest("11. REST API JSON Serialization & Utility", PatternTestSuite::testJsonSerialization);

        System.out.println("\n================================================================================");
        System.out.printf("🏁 TEST RUN COMPLETE: %d PASSED, %d FAILED%n", testsPassed, testsFailed);
        System.out.println("================================================================================\n");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, Runnable testFunc) {
        try {
            System.out.print("▶ Running: " + testName + " ... ");
            testFunc.run();
            System.out.println(" [PASSED ✓]");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println(" [FAILED ✗]");
            System.err.println("   Reason: " + t.getMessage());
            t.printStackTrace();
            testsFailed++;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("Assertion Failed: " + message + " (Expected: " + expected + ", Got: " + actual + ")");
        }
    }

    // 1. Factory Method Pattern Test
    private static void testFactoryPattern() {
        LibraryResource book = ResourceFactory.createBook("TB01", "Refactoring", "Martin Fowler", "978-0201485677", "Shelf-CS");
        assertTrue(book instanceof Book, "ResourceFactory should return instance of Book");
        assertEquals("PHYSICAL_BOOK", book.getResourceType(), "Book type mismatch");
        assertEquals(14, book.getMaxBorrowDays(), "Standard physical book should have 14 days loan period");

        LibraryResource ebook = ResourceFactory.createEBook("TEB01", "Design Patterns in C#", "Vince Huston", 5.4, "PDF");
        assertTrue(ebook instanceof EBook, "ResourceFactory should return instance of EBook");
        assertEquals("EBOOK", ebook.getResourceType(), "EBook type mismatch");
        assertEquals(21, ebook.getMaxBorrowDays(), "Digital eBook should have 21 days loan period");

        LibraryResource journal = ResourceFactory.createJournal("TJ01", "ACM Transactions", "ACM", 30, 2, true);
        assertTrue(journal instanceof Journal, "ResourceFactory should return instance of Journal");
        assertEquals("JOURNAL", journal.getResourceType(), "Journal type mismatch");
        assertEquals(7, journal.getMaxBorrowDays(), "Journal should have 7 days loan period");
    }

    // 2-7. Chain of Responsibility Tests
    private static void testChainRule1Membership() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();
        User inactive = new User("U_INACTIVE", "Inactive User", "in@lib.org", "000", false, 0.0, 0, "MEMBER");
        db.saveUser(inactive);
        Book b = new Book("TB_CHAIN1", "Test Book 1", "Author", "ISBN", "Shelf", true);
        db.saveResource(b);

        BorrowResult res = service.borrowResourceDetailed("U999", "U_INACTIVE", "TB_CHAIN1");
        assertTrue(!res.isSuccess(), "Inactive member should be rejected by Chain");
        assertEquals("MEMBERSHIP_INACTIVE", res.getErrorCode(), "Error code should be MEMBERSHIP_INACTIVE");
    }

    private static void testChainRule2Fine() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();
        User fined = new User("U_FINED", "Fined User", "fine@lib.org", "000", true, 25.00, 0, "MEMBER");
        db.saveUser(fined);
        Book b = new Book("TB_CHAIN2", "Test Book 2", "Author", "ISBN", "Shelf", true);
        db.saveResource(b);

        BorrowResult res = service.borrowResourceDetailed("U999", "U_FINED", "TB_CHAIN2");
        assertTrue(!res.isSuccess(), "User with fine > $10 should be rejected by Chain");
        assertEquals("FINE_LIMIT_EXCEEDED", res.getErrorCode(), "Error code should be FINE_LIMIT_EXCEEDED");
    }

    private static void testChainRule3BorrowLimit() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();
        User maxed = new User("U_MAXED", "Max Loans User", "max@lib.org", "000", true, 0.0, 5, "MEMBER");
        db.saveUser(maxed);
        Book b = new Book("TB_CHAIN3", "Test Book 3", "Author", "ISBN", "Shelf", true);
        db.saveResource(b);

        BorrowResult res = service.borrowResourceDetailed("U999", "U_MAXED", "TB_CHAIN3");
        assertTrue(!res.isSuccess(), "User with 5 active loans should be rejected by Chain");
        assertEquals("BORROW_LIMIT_EXCEEDED", res.getErrorCode(), "Error code should be BORROW_LIMIT_EXCEEDED");
    }

    private static void testChainRule4Availability() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();
        User validUser = new User("U_VALID4", "Valid User", "v4@lib.org", "000", true, 0.0, 0, "MEMBER");
        db.saveUser(validUser);
        Journal refOnly = new Journal("TJ_REF", "Reference Only Journal", "Press", 1, 1, true, true);
        db.saveResource(refOnly);

        BorrowResult res = service.borrowResourceDetailed("U999", "U_VALID4", "TJ_REF");
        assertTrue(!res.isSuccess(), "Reference only resource should not be borrowable");
    }

    private static void testChainRule5Reservation() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();
        User u1 = new User("U_RES_HOLDER", "Holder", "h@lib.org", "000", true, 0.0, 0, "MEMBER");
        User u2 = new User("U_RES_OTHER", "Other", "o@lib.org", "000", true, 0.0, 0, "MEMBER");
        db.saveUser(u1);
        db.saveUser(u2);
        Book b = new Book("TB_RES_CHECK", "Reserved Book", "Author", "ISBN", "Shelf", true);
        db.saveResource(b);

        service.reserveResource("U_RES_HOLDER", "TB_RES_CHECK");

        // u2 tries to borrow book held for u1
        BorrowResult res = service.borrowResourceDetailed("U999", "U_RES_OTHER", "TB_RES_CHECK");
        assertTrue(!res.isSuccess(), "Item reserved by another member should be blocked");
    }

    private static void testChainFullSuccess() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();
        User goodUser = new User("U_GOOD", "Good User", "good@lib.org", "000", true, 0.0, 0, "MEMBER");
        db.saveUser(goodUser);
        Book b = new Book("TB_GOOD", "Good Book", "Author", "ISBN", "Shelf", true);
        db.saveResource(b);

        BorrowResult res = service.borrowResourceDetailed("U999", "U_GOOD", "TB_GOOD");
        assertTrue(res.isSuccess(), "Eligible member should successfully borrow available resource");
        assertTrue(!b.isAvailable(), "Resource should be marked unavailable after borrow");
        assertEquals(1, goodUser.getCurrentBorrowedCount(), "User borrowed count should increment by 1");
    }

    // 8. Observer Pattern Test
    private static void testObserverPattern() {
        ReservationManager rm = ReservationManager.getInstance();
        List<String> receivedEvents = new ArrayList<>();

        Observer mockObserver = new Observer() {
            @Override
            public void update(String resourceId, String resourceTitle, String eventType, String details) {
                receivedEvents.add(eventType + ":" + resourceId);
            }
            @Override
            public String getObserverName() { return "TestObserver"; }
            @Override
            public String getObserverId() { return "OBS_01"; }
        };

        rm.registerObserver("RES_OBS_01", mockObserver);
        rm.notifyObservers("RES_OBS_01", "Reactive Systems", "RESOURCE_AVAILABLE", "Book is back in stock");

        assertEquals(1, receivedEvents.size(), "Observer should have received 1 event notification");
        assertEquals("RESOURCE_AVAILABLE:RES_OBS_01", receivedEvents.get(0), "Event payload mismatch");

        rm.removeObserver("RES_OBS_01", mockObserver);
    }

    // 9. Proxy Pattern Test
    private static void testProxyPattern() {
        LibraryService proxy = new LibraryServiceProxy();
        Database db = Database.getInstance();
        User normalMember = new User("U_NORMAL", "Normal Member", "norm@lib.org", "000", true, 0.0, 0, "MEMBER");
        db.saveUser(normalMember);

        // Attempt unauthorized action: Normal member trying to view another member's loans
        List<?> loans = proxy.getUserLoans("U_NORMAL", "U102");
        assertEquals(0, loans.size(), "Proxy should deny unauthorized cross-user loan inspection");

        // Verify Audit Log was recorded
        List<AuditLogEntry> logs = db.getAuditLogs();
        assertTrue(!logs.isEmpty(), "Proxy must record audit log entries in Database");
    }

    // 10. Bridge Pattern Test
    private static void testBridgePattern() {
        NotificationChannel email = new EmailChannel();
        NotificationChannel sms = new SMSChannel();
        NotificationChannel inApp = new InAppChannel();

        assertEquals("EMAIL", email.getChannelName(), "EmailChannel implementor name mismatch");
        assertEquals("SMS", sms.getChannelName(), "SMSChannel implementor name mismatch");
        assertEquals("IN_APP", inApp.getChannelName(), "InAppChannel implementor name mismatch");

        Notification notifEmail = new BookAvailableNotification(email);
        Notification notifSMS = new BookAvailableNotification(sms);
        Notification notifInApp = new BookAvailableNotification(inApp);

        // Execute sends without exceptions
        notifEmail.send("alice@example.com", "Your book 'Clean Architecture' is ready.");
        notifSMS.send("555-0102", "Your book 'Clean Architecture' is ready.");
        notifInApp.send("U101", "Your book 'Clean Architecture' is ready.");
    }

    // 11. JSON Serialization & Request Parsing
    private static void testJsonSerialization() {
        User u = new User("U_JSON", "Json Test", "j@lib.org", "555-1234", true, 5.0, 2, "MEMBER");
        String json = JsonUtils.userToJson(u);
        assertTrue(json.contains("\"userId\":\"U_JSON\""), "Serialized user JSON should contain userId");
        assertTrue(json.contains("\"outstandingFine\":5.00"), "Serialized user JSON should format fine");

        String sampleRequest = "{\"userId\":\"U101\",\"resourceId\":\"B001\",\"amount\":15.50}";
        Map<String, String> parsed = JsonUtils.parseSimpleJson(sampleRequest);
        assertEquals("U101", parsed.get("userId"), "Parsed JSON userId mismatch");
        assertEquals("B001", parsed.get("resourceId"), "Parsed JSON resourceId mismatch");
        assertEquals("15.50", parsed.get("amount"), "Parsed JSON amount mismatch");
    }
}
