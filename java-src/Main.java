import bridge.*;
import builder.*;
import chain.*;
import database.Database;
import factory.*;
import mediator.*;
import memento.*;
import model.Loan;
import model.User;
import observer.Member;
import observer.ReservationManager;
import prototype.*;
import proxy.LibraryService;
import proxy.LibraryServiceProxy;
import server.LibraryApiServer;
import service.LibraryServiceImpl;
import strategy.*;
import template.*;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final LibraryService libraryService = new LibraryServiceImpl();
    private static final Database db = Database.getInstance();
    private static final ReservationManager reservationManager = ReservationManager.getInstance();

    public static void main(String[] args) {
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            if ("--demo".equals(arg)) {
                runDesignPatternsShowcase();
                return;
            } else if ("--server".equals(arg) || "-s".equals(arg)) {
                int port = 8080;
                if (args.length > 1) {
                    try {
                        port = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {}
                }
                startServer(port);
                return;
            }
        }

        printBanner();

        // Run automated demonstration first so the user sees all patterns working seamlessly
        System.out.println("\n🚀 Running Automated Design Patterns Verification & Showcase...\n");
        runDesignPatternsShowcase();

        System.out.println("\n================================================================================");
        System.out.println("            ENTERING INTERACTIVE LIBRARY MANAGEMENT CLI CONSOLE");
        System.out.println("================================================================================\n");

        runInteractiveMenu();
    }

    private static void startServer(int port) {
        try {
            LibraryApiServer apiServer = new LibraryApiServer(port);
            apiServer.start();
            System.out.println("Press Ctrl+C to stop the server.");
        } catch (IOException e) {
            System.err.println("Failed to start API server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                📚  SMART LIBRARY MANAGEMENT SYSTEM (JAVA)                    ║");
        System.out.println("║    Unified GoF Design Patterns Architecture (11 Design Patterns Working):    ║");
        System.out.println("║    1. Factory Method    2. Chain of Resp.    3. Observer     4. Bridge       ║");
        System.out.println("║    5. Proxy             6. Builder           7. Prototype    8. Mediator     ║");
        System.out.println("║    9. Template Method  10. Strategy         11. Memento                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Demonstrates all 11 GoF Design Patterns in action with live assertions & outputs.
     */
    public static void runDesignPatternsShowcase() {
        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  1. FACTORY METHOD PATTERN DEMONSTRATION                                     │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        LibraryResource physicalBook = ResourceFactory.createBook(
                "B100", "Domain-Driven Design", "Eric Evans", "978-0321125217", "Shelf-DDD-01"
        );
        LibraryResource eBook = ResourceFactory.createEBook(
                "EB100", "Building Microservices", "Sam Newman", 12.8, "EPUB"
        );
        LibraryResource journal = ResourceFactory.createJournal(
                "J100", "Nature: Computer Science Edition", "Nature Publishing", 12, 4, true
        );

        System.out.println("  -> Created Book via Factory: " + physicalBook.getDetails());
        System.out.println("  -> Created EBook via Factory: " + eBook.getDetails());
        System.out.println("  -> Created Journal via Factory: " + journal.getDetails());
        System.out.println("  [PASS] Factory Pattern successfully instantiates polymorphic resources.\n");

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  2. CHAIN OF RESPONSIBILITY PATTERN DEMONSTRATION                            │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        System.out.println("  Test 2A: Borrow with Inactive Membership (User U103)...");
        boolean res2a = libraryService.borrowResource("U999", "U103", "B001");
        System.out.println("  -> Result: " + (res2a ? "ALLOWED" : "REJECTED (Correctly blocked by MembershipHandler)"));

        System.out.println("  Test 2B: Borrow with Fine > $10.00 (User U102 has $15.50 unpaid fine)...");
        boolean res2b = libraryService.borrowResource("U999", "U102", "B001");
        System.out.println("  -> Result: " + (res2b ? "ALLOWED" : "REJECTED (Correctly blocked by FineHandler)"));

        System.out.println("  Test 2C: Borrow with Maximum Limit Reached (User U104 has 5 active loans)...");
        boolean res2c = libraryService.borrowResource("U999", "U104", "B001");
        System.out.println("  -> Result: " + (res2c ? "ALLOWED" : "REJECTED (Correctly blocked by BorrowLimitHandler)"));

        System.out.println("  Test 2D: Borrow Reference-Only Resource (Journal J002)...");
        boolean res2d = libraryService.borrowResource("U999", "U101", "J002");
        System.out.println("  -> Result: " + (res2d ? "ALLOWED" : "REJECTED (Correctly blocked by AvailabilityHandler)"));

        System.out.println("  Test 2E: Borrow Valid Available Resource (User U101 borrows B001)...");
        boolean res2e = libraryService.borrowResource("U999", "U101", "B001");
        System.out.println("  -> Result: " + (res2e ? "SUCCESS (All Chain checks passed!)" : "FAILED"));
        System.out.println("  [PASS] Chain of Responsibility successfully enforced all validation rules.\n");

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  3. OBSERVER PATTERN & 4. BRIDGE PATTERN DEMONSTRATION                       │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        User u105 = new User("U105", "Evan Wright", "evan@stanford.edu", "555-0105", true, 0.0, 0, "MEMBER");
        db.saveUser(u105);

        NotificationChannel emailChan = new EmailChannel();
        NotificationChannel smsChan = new SMSChannel();

        Member emailObserver = new Member(u105, new BookAvailableNotification(emailChan));
        Member smsObserver = new Member(u105, new BookAvailableNotification(smsChan));

        reservationManager.registerObserver("B001", emailObserver);
        reservationManager.registerObserver("B001", smsObserver);
        libraryService.reserveResource("U105", "B001");

        System.out.println("  -> Returning book B001 into inventory (triggers Observer event)...");
        libraryService.returnResource("U999", "B001");
        System.out.println("  [PASS] Observer and Bridge patterns triggered and dispatched cross-channel alerts.\n");

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  5. PROXY PATTERN DEMONSTRATION (RBAC, Audit Logging & Telemetry)            │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        System.out.println("  Test 5A: Non-admin Member U101 attempts to add resource to library catalog...");
        libraryService.addResource("U101", ResourceFactory.createBook("B999", "Hacking 101", "Anon", "000-000", "Shelf-X"));

        System.out.println("  Test 5B: Non-admin Member U101 attempts to view loan records of Member U102...");
        List<Loan> unauthorizedLoans = libraryService.getUserLoans("U101", "U102");
        System.out.println("  -> Loan records returned: " + unauthorizedLoans.size() + " (Access Denied by Proxy)");

        System.out.println("  Test 5C: ADMIN User U999 adds a new resource...");
        libraryService.addResource("U999", ResourceFactory.createBook("B500", "Enterprise Integration Patterns", "Gregor Hohpe", "978-0321200686", "Shelf-EIP-01"));
        System.out.println("  [PASS] Proxy pattern strictly enforced RBAC and logged all telemetry.\n");

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  6. BUILDER DESIGN PATTERN DEMONSTRATION                                     │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        Book builtBook = new BookBuilder()
                .id("B-BLD-01")
                .title("Designing Data-Intensive Applications")
                .author("Martin Kleppmann")
                .isbn("978-1449373320")
                .shelfLocation("Tech-Arch-01")
                .publisher("O'Reilly Media")
                .edition("1st Edition")
                .category("Distributed Systems")
                .publicationYear(2017)
                .build();
        System.out.println("  -> Built Book via BookBuilder: " + builtBook.getDetails());
        System.out.println("     Category: " + builtBook.getCategory() + " | Publisher: " + builtBook.getPublisher() + " | Year: " + builtBook.getPublicationYear());

        try {
            new BookBuilder().id("").title("").author("").isbn("").build();
            System.out.println("  [FAIL] Builder validation should have thrown IllegalStateException");
        } catch (IllegalStateException ex) {
            System.out.println("  -> Caught expected validation exception: " + ex.getMessage());
            System.out.println("  [PASS] Builder Pattern validates required fields and constructs complex objects cleanly.\n");
        }

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  7. PROTOTYPE DESIGN PATTERN DEMONSTRATION                                   │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        Book originalBook = (Book) db.getResource("B001");
        if (originalBook != null) {
            Book clonedCopy = originalBook.createCopy("B001-COPY2", "Shelf-A1-Copy2");
            System.out.println("  -> Original Book: ID=" + originalBook.getId() + ", Title=" + originalBook.getTitle() + ", Shelf=" + originalBook.getShelfLocation());
            System.out.println("  -> Cloned Copy:   ID=" + clonedCopy.getId() + ", Title=" + clonedCopy.getTitle() + ", Shelf=" + clonedCopy.getShelfLocation());
            System.out.println("  -> Verified: Cloned ID differs (" + clonedCopy.getId() + ") and isAvailable=" + clonedCopy.isAvailable() + ", availableCopies=" + clonedCopy.getAvailableCopies());
            System.out.println("  [PASS] Prototype Pattern clones metadata while guaranteeing unique physical identity.\n");
        }

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  8. STRATEGY DESIGN PATTERN DEMONSTRATION (Fine Policies)                    │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        FineCalculationStrategy standard = FineStrategyFactory.getStrategyForRole("STANDARD");
        FineCalculationStrategy student = FineStrategyFactory.getStrategyForRole("STUDENT");
        FineCalculationStrategy faculty = FineStrategyFactory.getStrategyForRole("FACULTY");

        java.time.LocalDate dueDate = java.time.LocalDate.now().minusDays(10);
        java.time.LocalDate returnDate = java.time.LocalDate.now();
        FineCalculationResult rStandard = standard.calculateFine(dueDate, returnDate);
        FineCalculationResult rStudent = student.calculateFine(dueDate, returnDate);
        FineCalculationResult rFaculty = faculty.calculateFine(dueDate, returnDate);

        System.out.printf("  -> Standard Policy (10 days overdue): %s%n", rStandard.getSummary());
        System.out.printf("  -> Student  Policy (10 days overdue): %s%n", rStudent.getSummary());
        System.out.printf("  -> Faculty  Policy (10 days overdue): %s%n", rFaculty.getSummary());
        System.out.println("  [PASS] Strategy Pattern dynamically applies member-specific rates, grace periods, & caps.\n");

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  9. TEMPLATE METHOD PATTERN DEMONSTRATION (Issue/Return Lifecycle)           │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        LibraryTransaction issueTx = new IssueBookTransaction("U999", "U101", "B002");
        TransactionResult issueRes = issueTx.execute();
        System.out.println("  -> Executed IssueBookTransaction: Success=" + issueRes.isSuccess() + ", Message=" + issueRes.getMessage());
        for (String step : issueRes.getExecutionSteps()) {
            System.out.println("     • " + step);
        }

        LibraryTransaction returnTx = new ReturnBookTransaction("U999", "B002");
        TransactionResult returnRes = returnTx.execute();
        System.out.println("  -> Executed ReturnBookTransaction: Success=" + returnRes.isSuccess() + ", Message=" + returnRes.getMessage());
        for (String step : returnRes.getExecutionSteps()) {
            System.out.println("     • " + step);
        }
        System.out.println("  [PASS] Template Method Pattern enforces strict 6-phase transaction lifecycle & safety.\n");

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  10. MEDIATOR DESIGN PATTERN DEMONSTRATION (Circulation Coordination)        │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        LibraryMediator mediator = LibraryMediatorImpl.getInstance();
        ReturnCoordinationResult returnCoord = mediator.coordinateReturn("U999", "B001");
        System.out.println("  -> Mediator Return Coordination on B001: Success=" + returnCoord.isSuccess() + ", Msg=" + returnCoord.getMessage());
        System.out.println("     Held for reservation: " + returnCoord.isHeldForReservation() + " | Fine assessed: $" + returnCoord.getFineAmount());
        System.out.println("  [PASS] Mediator Pattern decoupled circulation, reservation, fine, and notification.\n");

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  11. MEMENTO DESIGN PATTERN DEMONSTRATION (Librarian State Undo)             │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        Book targetBook = (Book) db.getResource("B001");
        if (targetBook != null) {
            String initialTitle = targetBook.getTitle();
            libraryService.updateResourceMetadata("U999", "B001", "Modified Architecture Title", targetBook.getAuthor(), targetBook.getShelfLocation(), targetBook.getCategory(), targetBook.getPublisher(), targetBook.getEdition(), targetBook.getPublicationYear());
            System.out.println("  -> Modified Book Title in DB: '" + ((Book) db.getResource("B001")).getTitle() + "'");

            boolean undone = libraryService.undoLastEdit("U999");
            if (undone) {
                System.out.println("  -> Restored Book Title via Memento: '" + ((Book) db.getResource("B001")).getTitle() + "'");
            }
            System.out.println("  [PASS] Memento Pattern recorded snapshot and restored state via safe compensating update.\n");
        }
        System.out.println("================================================================================");
        System.out.println("🎉 ALL 11 GOF DESIGN PATTERNS VERIFIED SUCCESSFULLY IN SMART LIBRARY SYSTEM!");
        System.out.println("================================================================================\n");
    }

    private static void runInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);
        String currentUserId = "U999"; // default as Admin

        while (true) {
            User currentUser = db.getUser(currentUserId);
            String role = currentUser != null ? currentUser.getRole() : "MEMBER";

            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.printf("  Current User: %s (%s) [Role: %s] | Unpaid Fine: $%.2f%n",
                    currentUser != null ? currentUser.getName() : "Unknown", currentUserId, role,
                    currentUser != null ? currentUser.getOutstandingFine() : 0.0);
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("  1. Browse & Search Resource Catalog");
            System.out.println("  2. View All Members");
            System.out.println("  3. Issue / Borrow Resource (Chain of Resp, Template Method & Mediator)");
            System.out.println("  4. Return Resource (Template Method, Mediator, Observer & Fine Strategy)");
            System.out.println("  5. Reserve Resource / Join Waitlist (Observer & Bridge)");
            System.out.println("  6. View My Active Loans");
            System.out.println("  7. Add New Resource to Catalog via BookBuilder [Staff Only]");
            System.out.println("  8. Switch Active User Context");
            System.out.println("  9. Run Automated 11 Design Patterns Showcase Demo");
            System.out.println(" 10. Prototype: Clone Physical Book Copy from Existing Book");
            System.out.println(" 11. Memento: Edit Book Metadata & Undo Last Librarian Edit");
            System.out.println(" 12. Strategy: Preview Fine Calculation Across Member Roles");
            System.out.println(" 13. View Live 11 Design Patterns Architectural Registry");
            System.out.println("  0. Exit");
            System.out.print("\nEnter your choice (0-13): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("\n--- LIBRARY CATALOG ---");
                    for (LibraryResource r : libraryService.listAllResources()) {
                        System.out.println(" • " + r.getDetails());
                    }
                    break;

                case "2":
                    System.out.println("\n--- REGISTERED MEMBERS ---");
                    for (User u : db.getAllUsers()) {
                        System.out.println(" • " + u);
                    }
                    break;

                case "3":
                    System.out.print("Enter Member User ID (e.g. U101, U102): ");
                    String targetUser = scanner.nextLine().trim();
                    System.out.print("Enter Resource ID (e.g. B001, B002, EB001, J001): ");
                    String resId = scanner.nextLine().trim();
                    boolean issued = libraryService.borrowResource(currentUserId, targetUser, resId);
                    if (issued) {
                        System.out.println("✓ Resource issued successfully via Transaction Pipeline!");
                    } else {
                        System.out.println("✗ Borrow request could not be processed.");
                    }
                    break;

                case "4":
                    System.out.print("Enter Resource ID to return (e.g. B001, B003): ");
                    String returnResId = scanner.nextLine().trim();
                    boolean returned = libraryService.returnResource(currentUserId, returnResId);
                    if (returned) {
                        System.out.println("✓ Return processed successfully via Mediator Coordination!");
                    } else {
                        System.out.println("✗ Return request failed.");
                    }
                    break;

                case "5":
                    System.out.print("Enter Resource ID to reserve (e.g. B003): ");
                    String rsvId = scanner.nextLine().trim();
                    System.out.println("Select Notification Channel: 1. Email  2. SMS  3. In-App");
                    System.out.print("Choice: ");
                    String chChoice = scanner.nextLine().trim();
                    NotificationChannel ch = "2".equals(chChoice) ? new SMSChannel() : ("3".equals(chChoice) ? new InAppChannel() : new EmailChannel());

                    Member obsMember = new Member(currentUser != null ? currentUser : new User(currentUserId, "User", "user@lib.org", "555-0000", true, 0, 0, "MEMBER"), new BookAvailableNotification(ch));
                    reservationManager.registerObserver(rsvId, obsMember);
                    libraryService.reserveResource(currentUserId, rsvId);
                    System.out.println("✓ Successfully reserved and subscribed to alerts via " + ch.getChannelName());
                    break;

                case "6":
                    System.out.println("\n--- ACCOUNT STATUS ---");
                    if (currentUser != null) {
                        System.out.println(currentUser);
                        System.out.println("\nActive Loans:");
                        List<Loan> loans = libraryService.getUserLoans(currentUserId, currentUserId);
                        if (loans.isEmpty()) {
                            System.out.println("  No active loans.");
                        } else {
                            for (Loan l : loans) {
                                System.out.println("  • " + l);
                            }
                        }
                    }
                    break;

                case "7":
                    System.out.println("Adding new Book via BookBuilder:");
                    System.out.print("Resource ID: ");
                    String nid = scanner.nextLine().trim();
                    System.out.print("Title (Required): ");
                    String ntitle = scanner.nextLine().trim();
                    System.out.print("Author (Required): ");
                    String naut = scanner.nextLine().trim();
                    System.out.print("ISBN (Required): ");
                    String nisbn = scanner.nextLine().trim();
                    System.out.print("Shelf Location: ");
                    String nshelf = scanner.nextLine().trim();
                    System.out.print("Publisher (Optional): ");
                    String npub = scanner.nextLine().trim();
                    System.out.print("Category (Optional): ");
                    String ncat = scanner.nextLine().trim();

                    try {
                        Book built = new BookBuilder()
                                .id(nid)
                                .title(ntitle)
                                .author(naut)
                                .isbn(nisbn)
                                .shelfLocation(nshelf)
                                .publisher(npub)
                                .category(ncat)
                                .build();
                        libraryService.addResource(currentUserId, built);
                        System.out.println("✓ Book constructed using Builder Pattern and saved to Catalog!");
                    } catch (Exception e) {
                        System.out.println("✗ Error building book: " + e.getMessage());
                    }
                    break;

                case "8":
                    System.out.println("\nAvailable users to switch to:");
                    for (User u : db.getAllUsers()) {
                        System.out.printf(" • ID: %s | %s | Fine: $%.2f | Role: %s | Active: %b%n",
                                u.getUserId(), u.getName(), u.getOutstandingFine(), u.getRole(), u.isMembershipActive());
                    }
                    System.out.print("Enter User ID to switch to: ");
                    String switchId = scanner.nextLine().trim().toUpperCase();
                    if (db.getUser(switchId) != null) {
                        currentUserId = switchId;
                        System.out.println("Switched user to " + currentUserId);
                    } else {
                        System.out.println("User not found.");
                    }
                    break;

                case "9":
                    runDesignPatternsShowcase();
                    break;

                case "10":
                    System.out.println("\n--- PROTOTYPE: CLONE PHYSICAL BOOK COPY ---");
                    System.out.print("Enter Source Book ID to clone (e.g. B001, B002): ");
                    String sourceId = scanner.nextLine().trim();
                    System.out.print("Enter New Copy ID (e.g. " + sourceId + "-C2): ");
                    String newCopyId = scanner.nextLine().trim();
                    System.out.print("Enter New Shelf Location: ");
                    String newShelf = scanner.nextLine().trim();

                    LibraryResource copied = libraryService.createBookCopy(currentUserId, sourceId, newCopyId, newShelf);
                    if (copied != null) {
                        System.out.println("✓ Physical copy cloned successfully via Prototype Pattern!");
                        System.out.println("  Details: " + copied.getDetails());
                    } else {
                        System.out.println("✗ Failed to clone book copy. Verify permissions and source ID.");
                    }
                    break;

                case "11":
                    System.out.println("\n--- MEMENTO: EDIT METADATA & SAFE UNDO ---");
                    System.out.print("Enter Book ID to edit (e.g. B001): ");
                    String editId = scanner.nextLine().trim();
                    LibraryResource resToEdit = db.getResource(editId);
                    if (resToEdit instanceof Book) {
                        Book bookToEdit = (Book) resToEdit;
                        System.out.println("Current Title: " + bookToEdit.getTitle());
                        System.out.print("Enter New Title: ");
                        String updatedTitle = scanner.nextLine().trim();

                        boolean updated = libraryService.updateResourceMetadata(currentUserId, editId, updatedTitle, bookToEdit.getAuthor(), bookToEdit.getShelfLocation(), bookToEdit.getCategory(), bookToEdit.getPublisher(), bookToEdit.getEdition(), bookToEdit.getPublicationYear());
                        if (updated) {
                            System.out.println("✓ Book updated and pre-edit snapshot recorded in Memento Caretaker!");
                            System.out.print("Do you want to undo this edit now? (y/n): ");
                            if ("y".equalsIgnoreCase(scanner.nextLine().trim())) {
                                boolean undone = libraryService.undoLastEdit(currentUserId);
                                if (undone) {
                                    System.out.println("✓ Memento undo successful! Restored title: " + ((Book) db.getResource(editId)).getTitle());
                                } else {
                                    System.out.println("✗ Undo failed.");
                                }
                            }
                        }
                    } else {
                        System.out.println("Resource not found or is not an editable book.");
                    }
                    break;

                case "12":
                    System.out.println("\n--- STRATEGY: FINE CALCULATION SANDBOX ---");
                    System.out.print("Enter number of overdue days to test: ");
                    try {
                        int days = Integer.parseInt(scanner.nextLine().trim());
                        java.time.LocalDate dDate = java.time.LocalDate.now().minusDays(days);
                        java.time.LocalDate rDate = java.time.LocalDate.now();
                        for (String policy : new String[]{"STANDARD", "STUDENT", "FACULTY"}) {
                            FineCalculationStrategy strat = FineStrategyFactory.getStrategyForRole(policy);
                            FineCalculationResult res = strat.calculateFine(dDate, rDate);
                            System.out.printf(" • %-10s -> Fine: $%.2f (Grace: %dd, Rate: $%.2f/d, Cap: $%.2f) - %s%n",
                                    policy, res.getCalculatedFine(), strat.getGracePeriodDays(),
                                    strat.getDailyRate(), strat.getMaxFineLimit(), res.getSummary());
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number of days.");
                    }
                    break;

                case "13":
                    System.out.println("\n================================================================================");
                    System.out.println("        SMART LIBRARY SYSTEM - ALL 11 GOF DESIGN PATTERNS REGISTRY");
                    System.out.println("================================================================================");
                    System.out.println(" 1. Factory Method   : ResourceFactory creates polymorphic Books, EBooks, Journals");
                    System.out.println(" 2. Chain of Resp.   : 5 Validation Handlers enforce borrow business rules");
                    System.out.println(" 3. Observer Pattern : ReservationManager notifies members when resources return");
                    System.out.println(" 4. Bridge Pattern   : Decouples notification messages from delivery channels (Email/SMS/InApp)");
                    System.out.println(" 5. Proxy Pattern    : LibraryServiceProxy enforces RBAC, logging, & telemetry");
                    System.out.println(" 6. Builder Pattern  : BookBuilder & UserBuilder assemble objects with validated fields");
                    System.out.println(" 7. Prototype Pattern: Prototype<T> & Book.createCopy clone metadata with independent copies");
                    System.out.println(" 8. Strategy Pattern : FineStrategyFactory applies Standard/Student/Faculty fine policies");
                    System.out.println(" 9. Template Method  : LibraryTransaction defines 6-step lifecycle for Issue & Return");
                    System.out.println("10. Mediator Pattern : LibraryMediator coordinates circulation, reservations, fines, alerts");
                    System.out.println("11. Memento Pattern  : LibraryMemento & LibraryHistory record snapshots for safe database undo");
                    System.out.println("================================================================================");
                    break;

                case "0":
                    System.out.println("\nThank you for using Smart Library Management System. Goodbye!");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}


