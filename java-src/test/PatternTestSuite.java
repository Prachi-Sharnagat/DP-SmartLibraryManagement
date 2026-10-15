package test;

import bridge.*;
import builder.BookBuilder;
import builder.UserBuilder;
import chain.ValidationHandler;
import database.Database;
import factory.Book;
import factory.EBook;
import factory.Journal;
import factory.LibraryResource;
import factory.ResourceFactory;
import mediator.LibraryMediatorImpl;
import mediator.ReturnCoordinationResult;
import memento.LibraryHistory;
import memento.ResourceMemento;
import model.AuditLogEntry;
import model.BorrowResult;
import model.Loan;
import model.ReturnResult;
import model.User;
import observer.Member;
import observer.Observer;
import observer.ReservationManager;
import prototype.CatalogTemplateRegistry;
import proxy.LibraryService;
import proxy.LibraryServiceProxy;
import server.JsonUtils;
import service.LibraryServiceImpl;
import strategy.*;
import template.IssueBookTransaction;
import template.ReturnBookTransaction;
import template.TransactionResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive Automated Test Suite verifying all 11 GoF Design Patterns and system logic:
 * 1. Factory Method Pattern
 * 2-7. Chain of Responsibility (5 rules + full success)
 * 8. Observer Pattern
 * 9. Proxy Pattern (RBAC & Audit Telemetry)
 * 10. Bridge Pattern (Multi-Channel Notifications)
 * 11. REST API JSON Serialization
 * 12. Builder Pattern (Valid Book & User Construction)
 * 13. Builder Pattern (Validation Failure on Missing Mandatory Fields)
 * 14. Prototype Pattern (Independent Book Copy with Unique ID & Mutable State)
 * 15. Prototype Pattern (Catalog Template Registry Deep Cloning)
 * 16. Strategy Pattern (Standard, Student, Faculty Fines & Grace Periods)
 * 17. Template Method Pattern (Standard IssueBookTransaction Workflow)
 * 18. Template Method Pattern (Standard ReturnBookTransaction Workflow)
 * 19. Template Method Pattern (Rollback Safety on Transaction Failure)
 * 20. Mediator Pattern (Circulation Return Coordination across Colleagues)
 * 21. Memento Pattern (Snapshot, Compensating Rollback & Irreversible Actions Guard)
 */
public class PatternTestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("          🧪 SMART LIBRARY MANAGEMENT SYSTEM - DESIGN PATTERNS TEST SUITE       ");
        System.out.println("          Testing all 11 GoF Design Patterns & System Workflows                 ");
        System.out.println("================================================================================\n");

        // Existing 11 Tests (Preserved)
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

        // New 10 Tests for the 6 additional Design Patterns
        runTest("12. Builder Pattern - Valid Book & User Construction", PatternTestSuite::testBuilderPatternValid);
        runTest("13. Builder Pattern - Rejection of Missing Mandatory Fields", PatternTestSuite::testBuilderPatternValidation);
        runTest("14. Prototype Pattern - Independent Book Copy Creation with Unique ID", PatternTestSuite::testPrototypePatternCopy);
        runTest("15. Prototype Pattern - Catalog Template Registry Cloning", PatternTestSuite::testPrototypePatternCatalogTemplate);
        runTest("16. Strategy Pattern - Fine Calculation for Standard, Student, and Faculty", PatternTestSuite::testStrategyPatternFines);
        runTest("17. Template Method Pattern - Standard IssueBookTransaction Workflow", PatternTestSuite::testTemplateMethodIssueWorkflow);
        runTest("18. Template Method Pattern - Standard ReturnBookTransaction Workflow", PatternTestSuite::testTemplateMethodReturnWorkflow);
        runTest("19. Template Method Pattern - Rollback Safety on Transaction Failure", PatternTestSuite::testTemplateMethodRollbackOnFailure);
        runTest("20. Mediator Pattern - Circulation Return Coordination across Colleagues", PatternTestSuite::testMediatorCirculationCoordination);
        runTest("21. Memento Pattern - State Snapshot, Compensating Rollback & Safety Guard", PatternTestSuite::testMementoPatternUndoAndReversibility);

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
            System.out.println("   Reason: " + t.getMessage());
            t.printStackTrace(System.err);
            testsFailed++;
        }
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + msg);
        }
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(String.format("Assertion Failed: %s (Expected: %s, Got: %s)", msg, expected, actual));
    }

    // ==========================================
    // EXISTING TESTS (1 - 11)
    // ==========================================

    private static void testFactoryPattern() {
        LibraryResource book = ResourceFactory.createBook("TB01", "Design Patterns", "GoF", "978-0201633610", "Shelf-A");
        LibraryResource ebook = ResourceFactory.createEBook("TEB01", "Effective Java", "Joshua Bloch", 12.5, "PDF");
        LibraryResource journal = ResourceFactory.createJournal("TJ01", "IEEE Software", "IEEE", 40, 2, false);

        assertEquals("PHYSICAL_BOOK", book.getResourceType(), "Book resource type mismatch");
        assertEquals("EBOOK", ebook.getResourceType(), "EBook resource type mismatch");
        assertEquals("JOURNAL", journal.getResourceType(), "Journal resource type mismatch");

        assertTrue(book instanceof Book, "Should instantiate Book class");
        assertTrue(ebook instanceof EBook, "Should instantiate EBook class");
        assertTrue(journal instanceof Journal, "Should instantiate Journal class");
    }

    private static void testChainRule1Membership() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();
        User inactive = new User("U_INACTIVE", "Inactive User", "i@lib.org", "000", false, 0.0, 0, "MEMBER");
        db.saveUser(inactive);
        Book b = new Book("TB_CHAIN1", "Chain Test 1", "Author", "ISBN", "Shelf");
        db.saveResource(b);

        BorrowResult res = service.borrowResourceDetailed("U999", "U_INACTIVE", "TB_CHAIN1");
        assertTrue(!res.isSuccess(), "Inactive member should be rejected");
        assertEquals("MEMBERSHIP_INACTIVE", res.getErrorCode(), "Error code should be MEMBERSHIP_INACTIVE");
    }

    private static void testChainRule2Fine() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();
        User fined = new User("U_FINED", "Fined User", "f@lib.org", "000", true, 25.00, 0, "MEMBER");
        db.saveUser(fined);
        Book b = new Book("TB_CHAIN2", "Chain Test 2", "Author", "ISBN", "Shelf");
        db.saveResource(b);

        BorrowResult res = service.borrowResourceDetailed("U999", "U_FINED", "TB_CHAIN2");
        assertTrue(!res.isSuccess(), "Member with fine > $10 should be rejected");
        assertEquals("FINE_LIMIT_EXCEEDED", res.getErrorCode(), "Error code should be FINE_LIMIT_EXCEEDED");
    }

    private static void testChainRule3BorrowLimit() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();
        User maxed = new User("U_MAXED", "Maxed User", "m@lib.org", "000", true, 0.0, 5, "MEMBER");
        db.saveUser(maxed);
        Book b = new Book("TB_CHAIN3", "Chain Test 3", "Author", "ISBN", "Shelf");
        db.saveResource(b);

        BorrowResult res = service.borrowResourceDetailed("U999", "U_MAXED", "TB_CHAIN3");
        assertTrue(!res.isSuccess(), "Member with 5 items should be rejected");
        assertEquals("BORROW_LIMIT_EXCEEDED", res.getErrorCode(), "Error code should be BORROW_LIMIT_EXCEEDED");
    }

    private static void testChainRule4Availability() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();
        User validUser = new User("U_VALID4", "Valid User", "v@lib.org", "000", true, 0.0, 0, "MEMBER");
        db.saveUser(validUser);
        Journal refJournal = new Journal("TJ_REF", "Reference Only Journal", "Publisher", 1, 1, true);
        db.saveResource(refJournal);

        BorrowResult res = service.borrowResourceDetailed("U999", "U_VALID4", "TJ_REF");
        assertTrue(!res.isSuccess(), "Reference only item should be rejected");
        assertEquals("RESOURCE_UNAVAILABLE", res.getErrorCode(), "Error code should be RESOURCE_UNAVAILABLE");
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
        assertEquals(1, db.getUser("U_GOOD").getCurrentBorrowedCount(), "User borrowed count should increment by 1");
    }

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
        rm.notifyObservers("RES_OBS_01", "Reactive Systems", "RESOURCE_AVAILABLE", "Event 2");
        assertEquals(1, receivedEvents.size(), "Removed observer should not receive subsequent events");
    }

    private static void testProxyPattern() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();
        User normal = new User("U_NORMAL", "Normal User", "n@lib.org", "000", true, 0.0, 0, "MEMBER");
        db.saveUser(normal);

        List<Loan> loans = service.getUserLoans("U_NORMAL", "U102");
        assertEquals(0, loans.size(), "Non-admin member should be denied viewing other member's loans");

        List<AuditLogEntry> logs = db.getAuditLogs();
        assertTrue(!logs.isEmpty(), "Audit log entry should be created by proxy");
        boolean hasDeniedLog = logs.stream().anyMatch(l -> "VIEW_LOANS_DENIED".equals(l.getAction()));
        assertTrue(hasDeniedLog, "Audit log should record VIEW_LOANS_DENIED");
    }

    private static void testBridgePattern() {
        NotificationChannel email = new EmailChannel();
        NotificationChannel sms = new SMSChannel();
        NotificationChannel inApp = new InAppChannel();

        Notification bookAvailEmail = new BookAvailableNotification(email);
        Notification bookAvailSMS = new BookAvailableNotification(sms);
        Notification dueInApp = new DueDateNotification(inApp);

        User alice = new User("U_ALICE", "Alice", "alice@example.com", "555-0102", true, 0.0, 0, "MEMBER");
        User bob = new User("U_BOB", "Bob", "bob@example.com", "555-0103", true, 0.0, 0, "MEMBER");

        bookAvailEmail.send(alice, "Your book 'Clean Architecture' is ready.");
        bookAvailSMS.send(alice, "Your book 'Clean Architecture' is ready.");
        dueInApp.send(bob, "Your book 'Clean Architecture' is due tomorrow.");
    }

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

    // ==========================================
    // 6 NEW DESIGN PATTERNS TESTS (12 - 21)
    // ==========================================

    // 12. Builder Pattern: Valid Book & User Construction
    private static void testBuilderPatternValid() {
        Book book = new BookBuilder()
                .id("B_BLD_01")
                .title("Software Architecture: The Hard Parts")
                .author("Neal Ford & Mark Richards")
                .isbn("978-1492086895")
                .shelfLocation("Shelf-ARCH-01")
                .publisher("O'Reilly Media")
                .edition("1st Edition")
                .category("Architecture")
                .publicationYear(2021)
                .totalCopies(3)
                .availableCopies(3)
                .build();

        assertEquals("B_BLD_01", book.getId(), "Book ID mismatch");
        assertEquals("Software Architecture: The Hard Parts", book.getTitle(), "Book Title mismatch");
        assertEquals("Neal Ford & Mark Richards", book.getAuthor(), "Book Author mismatch");
        assertEquals("978-1492086895", book.getIsbn(), "Book ISBN mismatch");
        assertEquals("O'Reilly Media", book.getPublisher(), "Book Publisher mismatch");
        assertEquals("1st Edition", book.getEdition(), "Book Edition mismatch");
        assertEquals("Architecture", book.getCategory(), "Book Category mismatch");
        assertEquals(2021, book.getPublicationYear(), "Book Year mismatch");
        assertEquals(3, book.getTotalCopies(), "Book Total Copies mismatch");

        User user = new UserBuilder()
                .userId("U_BLD_01")
                .name("Grace Hopper")
                .email("grace@navy.mil")
                .phone("555-0909")
                .role("FACULTY")
                .department("Computer Engineering")
                .semester("Senior")
                .maxLoans(8)
                .build();

        assertEquals("U_BLD_01", user.getUserId(), "User ID mismatch");
        assertEquals("Grace Hopper", user.getName(), "User Name mismatch");
        assertEquals("grace@navy.mil", user.getEmail(), "User Email mismatch");
        assertEquals("FACULTY", user.getRole(), "User Role mismatch");
        assertEquals("Computer Engineering", user.getDepartment(), "User Department mismatch");
        assertEquals(8, user.getMaxLoans(), "User Max Loans mismatch");
    }

    // 13. Builder Pattern: Rejection of Missing Mandatory Fields
    private static void testBuilderPatternValidation() {
        // Missing title
        try {
            new BookBuilder().id("B_ERR_1").author("Author").isbn("ISBN-001").build();
            throw new AssertionError("Should have failed: Book title is mandatory");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Book Title is required"), "Validation message should mention Title");
        }

        // Missing author
        try {
            new BookBuilder().id("B_ERR_2").title("Some Title").isbn("ISBN-002").build();
            throw new AssertionError("Should have failed: Book author is mandatory");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Book Author is required"), "Validation message should mention Author");
        }

        // Missing ISBN
        try {
            new BookBuilder().id("B_ERR_3").title("Some Title").author("Author").build();
            throw new AssertionError("Should have failed: Book ISBN is mandatory");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Book ISBN is required"), "Validation message should mention ISBN");
        }

        // Invalid user email
        try {
            new UserBuilder().userId("U_ERR_1").name("Name").email("not-an-email").build();
            throw new AssertionError("Should have failed: User email must be valid");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Valid User Email is required"), "Validation message should mention Email");
        }
    }

    // 14. Prototype Pattern: Independent Book Copy Creation with Unique ID
    private static void testPrototypePatternCopy() {
        Book source = new BookBuilder()
                .id("B_PROTO_SRC")
                .title("Operating Systems: Three Easy Pieces")
                .author("Remzi Arpaci-Dusseau")
                .isbn("978-1985086593")
                .shelfLocation("Stacks-OS-01")
                .publisher("Arpaci-Dusseau Books")
                .edition("Version 1.0")
                .category("Systems")
                .publicationYear(2018)
                .isAvailable(true)
                .build();

        // Create independent copy with unique ID
        Book copy = source.createCopy("B_PROTO_COPY_2", "Stacks-OS-Reserve");

        assertEquals("B_PROTO_COPY_2", copy.getId(), "Copy ID must be unique");
        assertEquals(source.getTitle(), copy.getTitle(), "Cloned copy must retain original title");
        assertEquals(source.getAuthor(), copy.getAuthor(), "Cloned copy must retain original author");
        assertEquals(source.getIsbn(), copy.getIsbn(), "Cloned copy must retain original ISBN");
        assertEquals("Stacks-OS-Reserve", copy.getShelfLocation(), "Copy must support its own shelf location");
        assertTrue(copy.isAvailable(), "New physical copy should be available");

        // Mutate copy state independently
        copy.setAvailable(false);
        assertTrue(!copy.isAvailable(), "Copy should be marked checked out");
        assertTrue(source.isAvailable(), "Source template book must remain completely unaffected by copy checkout");

        // Prevent primary key collision (copy ID cannot equal template ID)
        try {
            source.createCopy("B_PROTO_SRC", "Shelf-X");
            throw new AssertionError("Should reject creating copy with duplicate primary key ID");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("must differ"), "Must reject duplicate primary key ID");
        }
    }

    // 15. Prototype Pattern: Catalog Template Registry Cloning
    private static void testPrototypePatternCatalogTemplate() {
        CatalogTemplateRegistry registry = CatalogTemplateRegistry.getInstance();
        Book clonedBook = registry.cloneFromTemplate("CS_TEXTBOOK", "CS-990", "Distributed Computing in Practice", "978-0138888888");

        assertEquals("CS-990", clonedBook.getId(), "Cloned ID mismatch");
        assertEquals("Distributed Computing in Practice", clonedBook.getTitle(), "Cloned Title mismatch");
        assertEquals("978-0138888888", clonedBook.getIsbn(), "Cloned ISBN mismatch");
        assertEquals("Computer Science", clonedBook.getCategory(), "Template metadata (category) must be preserved");
        assertEquals("MIT Press", clonedBook.getPublisher(), "Template metadata (publisher) must be preserved");
        assertTrue(clonedBook.isAvailable(), "Cloned template record must be initialized as available");
    }

    // 16. Strategy Pattern: Fine Calculation for Standard, Student, and Faculty
    private static void testStrategyPatternFines() {
        FineCalculationStrategy standard = new StandardFineStrategy();
        FineCalculationStrategy student = new StudentFineStrategy();
        FineCalculationStrategy faculty = new FacultyFineStrategy();

        LocalDate dueDate = LocalDate.now().minusDays(10);
        LocalDate returnDate = LocalDate.now();

        // 10 overdue days:
        // Standard: 10 - 1 grace = 9 billable days * $1.00 = $9.00
        FineCalculationResult rStandard = standard.calculateFine(dueDate, returnDate);
        assertEquals(10, rStandard.getDaysOverdue(), "Standard overdue days mismatch");
        assertEquals(1, rStandard.getGraceDaysApplied(), "Standard grace period mismatch");
        assertEquals(9, rStandard.getBillableDays(), "Standard billable days mismatch");
        assertEquals(9.00, rStandard.getCalculatedFine(), "Standard calculated fine mismatch");

        // Student: 10 - 3 grace = 7 billable days * $0.50 = $3.50
        FineCalculationResult rStudent = student.calculateFine(dueDate, returnDate);
        assertEquals(10, rStudent.getDaysOverdue(), "Student overdue days mismatch");
        assertEquals(3, rStudent.getGraceDaysApplied(), "Student grace period mismatch");
        assertEquals(7, rStudent.getBillableDays(), "Student billable days mismatch");
        assertEquals(3.50, rStudent.getCalculatedFine(), "Student calculated fine mismatch");

        // Faculty within grace: 5 overdue days -> 0 billable days, $0.00 fine
        LocalDate facultyDue = LocalDate.now().minusDays(5);
        FineCalculationResult rFacultyInGrace = faculty.calculateFine(facultyDue, returnDate);
        assertEquals(0.00, rFacultyInGrace.getCalculatedFine(), "Faculty within 7-day grace period should owe $0.00");

        // Faculty after grace: 10 - 7 grace = 3 billable days * $0.10 = $0.30
        FineCalculationResult rFaculty = faculty.calculateFine(dueDate, returnDate);
        assertEquals(0.30, Math.round(rFaculty.getCalculatedFine() * 100.0) / 100.0, "Faculty calculated fine mismatch");

        // On-time return (0 overdue days)
        FineCalculationResult rOnTime = standard.calculateFine(LocalDate.now(), LocalDate.now());
        assertEquals(0.00, rOnTime.getCalculatedFine(), "On-time return should have $0.00 fine");

        // Maximum Fine Cap: Student with 100 overdue days -> capped at $20.00
        LocalDate severeDue = LocalDate.now().minusDays(100);
        FineCalculationResult rStudentCapped = student.calculateFine(severeDue, returnDate);
        assertTrue(rStudentCapped.isCappedAtMax(), "Severe overdue should trigger maximum fine cap");
        assertEquals(20.00, rStudentCapped.getCalculatedFine(), "Student fine should be capped at $20.00");

        // Strategy Selector Factory
        User studentUser = new User("U_STU", "Student", "s@edu.org", "000", true, 0.0, 0, "STUDENT");
        FineCalculationStrategy resolvedStrategy = FineStrategyFactory.getStrategy(studentUser);
        assertEquals("Student Subsidized Fine Policy", resolvedStrategy.getStrategyName(), "Strategy factory must resolve Student policy");
    }

    // 17. Template Method Pattern: Standard IssueBookTransaction Workflow
    private static void testTemplateMethodIssueWorkflow() {
        Database db = Database.getInstance();
        User borrower = new UserBuilder().userId("U_TMPL_1").name("Template User").email("t@lib.org").build();
        db.saveUser(borrower);

        Book book = new BookBuilder().id("B_TMPL_1").title("Code Complete").author("Steve McConnell").isbn("978-0735619678").build();
        db.saveResource(book);

        IssueBookTransaction tx = new IssueBookTransaction("U999", "U_TMPL_1", "B_TMPL_1");
        TransactionResult result = tx.execute();

        assertTrue(result.isSuccess(), "IssueBookTransaction should succeed for eligible user");
        assertEquals("ISSUE_BOOK", result.getTransactionType(), "Transaction type mismatch");
        assertTrue(!book.isAvailable(), "Book must be marked unavailable after issue transaction");
        assertEquals(1, db.getUser("U_TMPL_1").getCurrentBorrowedCount(), "User borrowed count must increment in database");

        // Verify sequence of execution steps logged by template method
        List<String> steps = result.getExecutionSteps();
        assertTrue(steps.size() >= 5, "Template method should record all sequential lifecycle steps");
        assertTrue(steps.stream().anyMatch(s -> s.contains("Validating")), "Must execute validation step");
        assertTrue(steps.stream().anyMatch(s -> s.contains("Preparing")), "Must execute preparation step");
        assertTrue(steps.stream().anyMatch(s -> s.contains("Executing Core Business")), "Must execute core mutation step");
        assertTrue(steps.stream().anyMatch(s -> s.contains("Persisting Changes")), "Must execute persistence step");
        assertTrue(steps.stream().anyMatch(s -> s.contains("Audit Log")), "Must execute audit logging step");
    }

    // 18. Template Method Pattern: Standard ReturnBookTransaction Workflow
    private static void testTemplateMethodReturnWorkflow() {
        Database db = Database.getInstance();
        LibraryService service = new LibraryServiceImpl();

        User borrower = new UserBuilder().userId("U_TMPL_RET").name("Returner").email("r@lib.org").build();
        db.saveUser(borrower);

        Book book = new BookBuilder().id("B_TMPL_RET").title("Refactoring").author("Martin Fowler").isbn("978-0134757599").build();
        db.saveResource(book);

        // Issue book first
        service.borrowResourceDetailed("U999", "U_TMPL_RET", "B_TMPL_RET");

        // Execute return transaction
        ReturnBookTransaction returnTx = new ReturnBookTransaction("U999", "B_TMPL_RET");
        TransactionResult result = returnTx.execute();

        assertTrue(result.isSuccess(), "ReturnBookTransaction should succeed for active loan");
        assertTrue(book.isAvailable(), "Book must be marked available in catalog after return");
        assertEquals(0, db.getUser("U_TMPL_RET").getCurrentBorrowedCount(), "User borrowed count should decrement to 0");
    }

    // 19. Template Method Pattern: Rollback Safety on Transaction Failure
    private static void testTemplateMethodRollbackOnFailure() {
        Database db = Database.getInstance();
        User user = new UserBuilder().userId("U_ROLL_1").name("Rollback User").email("roll@lib.org").build();
        db.saveUser(user);

        Book unavailableBook = new BookBuilder().id("B_ROLL_1").title("Unavailable Book").author("Anon").isbn("ISBN-000").isAvailable(false).build();
        db.saveResource(unavailableBook);

        IssueBookTransaction failTx = new IssueBookTransaction("U999", "U_ROLL_1", "B_ROLL_1");
        TransactionResult res = failTx.execute();

        assertTrue(!res.isSuccess(), "Transaction on unavailable item must fail");
        assertEquals("RESOURCE_UNAVAILABLE", res.getErrorCode(), "Error code should indicate resource unavailable");
        assertEquals(0, db.getUser("U_ROLL_1").getCurrentBorrowedCount(), "User count must not increment after failed transaction");
        assertTrue(!unavailableBook.isAvailable(), "Book availability must not be corrupted");
        assertTrue(res.getExecutionSteps().stream().anyMatch(s -> s.contains("Rollback executed") || s.contains("Transaction Aborted")), "Rollback log must be recorded");
    }

    // 20. Mediator Pattern: Circulation Return Coordination across Colleagues
    private static void testMediatorCirculationCoordination() {
        Database db = Database.getInstance();
        LibraryMediatorImpl mediator = LibraryMediatorImpl.getInstance();

        User uBorrower = new UserBuilder().userId("U_MED_BORROW").name("Borrower").email("b@med.org").role("STUDENT").build();
        User uWaiting = new UserBuilder().userId("U_MED_WAIT").name("Waitlist Patron").email("w@med.org").role("MEMBER").build();
        db.saveUser(uBorrower);
        db.saveUser(uWaiting);

        Book book = new BookBuilder().id("B_MED_01").title("Pragmatic Programmer").author("David Thomas").isbn("978-0135957059").isAvailable(false).build();
        db.saveResource(book);

        // Active loan overdue by 8 days: Student has 3 days grace -> 5 billable days * $0.50 = $2.50 fine
        Loan loan = new Loan("LN-MED-01", "U_MED_BORROW", "B_MED_01", LocalDate.now().minusDays(22), LocalDate.now().minusDays(8));
        db.saveLoan(loan);

        // Waiting user reserves this book
        db.addReservation(new model.ReservationRecord("RES-MED-01", "B_MED_01", "Pragmatic Programmer", "U_MED_WAIT", "Waitlist Patron"));

        ReturnCoordinationResult coordinationResult = mediator.coordinateReturn("U999", "B_MED_01");

        assertTrue(coordinationResult.isSuccess(), "Mediator should coordinate return successfully");
        assertEquals(2.50, coordinationResult.getFineAmount(), "Mediator + Strategy should assess $2.50 overdue fine");
        assertTrue(coordinationResult.isHeldForReservation(), "Book should be placed on hold for reserving patron");
        assertEquals("U_MED_WAIT", coordinationResult.getNextReservedUserId(), "Reserving user ID should match waitlist member");
        assertTrue(!book.isAvailable(), "Held book must remain checked-out / unavailable for general checkout");
        assertTrue(loan.isReturned(), "Circulation colleague must mark active loan returned");
    }

    // 21. Memento Pattern: State Snapshot, Compensating Rollback & Safety Guard
    private static void testMementoPatternUndoAndReversibility() {
        LibraryService service = new LibraryServiceImpl();
        Database db = Database.getInstance();

        Book initialBook = new BookBuilder()
                .id("B_MEM_01")
                .title("Introduction to Modern Cryptography")
                .author("Jonathan Katz")
                .isbn("978-1466570269")
                .shelfLocation("Stacks-Crypto-01")
                .category("Cryptography")
                .publisher("CRC Press")
                .edition("2nd Edition")
                .publicationYear(2014)
                .build();
        db.saveResource(initialBook);

        // Step 1: Update metadata (saving pre-mutation state into Memento)
        boolean updated = service.updateResourceMetadata(
                "U999",
                "B_MEM_01",
                "Advanced Hacking and Penetration",
                "Anonymous Hacker",
                "Locked-Vault-01",
                "Security",
                "Dark Press",
                "Unauthorized 3rd",
                2026
        );
        assertTrue(updated, "Metadata update should succeed");

        Book modifiedBook = (Book) db.getResource("B_MEM_01");
        assertEquals("Advanced Hacking and Penetration", modifiedBook.getTitle(), "Database should reflect edited title");
        assertEquals("Locked-Vault-01", modifiedBook.getShelfLocation(), "Database should reflect edited shelf location");

        // Step 2: Undo the edit via Memento compensating database update
        boolean undone = service.undoLastEdit("U999");
        assertTrue(undone, "Undo action should successfully revert changes");

        Book restoredBook = (Book) db.getResource("B_MEM_01");
        assertEquals("Introduction to Modern Cryptography", restoredBook.getTitle(), "Compensating update must restore original title");
        assertEquals("Jonathan Katz", restoredBook.getAuthor(), "Compensating update must restore original author");
        assertEquals("Stacks-Crypto-01", restoredBook.getShelfLocation(), "Compensating update must restore original shelf");

        // Step 3: Irreversible actions safety guard
        assertTrue(!LibraryHistory.isActionReversible("FINE_PAYMENT"), "Financial payment must be marked irreversible");
        assertTrue(!LibraryHistory.isActionReversible("EXTERNAL_NOTIFICATION_SENT"), "Dispatched external message must be marked irreversible");

        try {
            LibraryHistory.validateReversibility("FINE_PAYMENT");
            throw new AssertionError("Should reject attempting to undo irreversible action");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage().contains("Irreversible Action"), "Must reject undo for payment");
        }
    }
}
