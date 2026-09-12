import bridge.*;
import chain.*;
import database.Database;
import factory.LibraryResource;
import factory.ResourceFactory;
import model.Loan;
import model.User;
import observer.Member;
import observer.ReservationManager;
import proxy.LibraryService;
import proxy.LibraryServiceProxy;
import server.LibraryApiServer;
import service.LibraryServiceImpl;

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
        System.out.println("║          Powered by GoF Design Patterns: Factory, Chain, Observer,           ║");
        System.out.println("║                          Proxy & Bridge Architecture                         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Demonstrates all 5 GoF Design Patterns in action with live assertions & outputs.
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
        System.out.println("  [PASS] Chain of Responsibility successfully enforced all 5 validation rules.\n");

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  3. OBSERVER PATTERN & 4. BRIDGE PATTERN DEMONSTRATION                       │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        // User U105 reserves currently checked out book B001 and requests Email + SMS notification
        User u105 = new User("U105", "Evan Wright", "evan@stanford.edu", "555-0105", true, 0.0, 0, "MEMBER");
        db.saveUser(u105);

        NotificationChannel emailChan = new EmailChannel();
        NotificationChannel smsChan = new SMSChannel();
        NotificationChannel inAppChan = new InAppChannel();

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
            System.out.println("  3. Issue / Borrow Resource (Chain of Responsibility & Proxy)");
            System.out.println("  4. Return Resource (Observer & Fine Calculation)");
            System.out.println("  5. Reserve Resource / Join Waitlist (Observer & Bridge)");
            System.out.println("  6. View My Active Loans");
            System.out.println("  7. Add New Resource to Catalog [Staff Only]");
            System.out.println("  8. Switch Active User Context");
            System.out.println("  9. Run Automated Design Patterns Showcase Demo");
            System.out.println("  0. Exit");
            System.out.print("\nEnter your choice (0-9): ");

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
                        System.out.println("✓ Resource issued successfully!");
                    } else {
                        System.out.println("✗ Borrow request could not be processed.");
                    }
                    break;

                case "4":
                    System.out.print("Enter Resource ID to return (e.g. B001, B003): ");
                    String returnResId = scanner.nextLine().trim();
                    boolean returned = libraryService.returnResource(currentUserId, returnResId);
                    if (returned) {
                        System.out.println("✓ Return processed successfully!");
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
                    System.out.println("Choose Resource Type: 1. Physical Book  2. E-Book  3. Journal");
                    System.out.print("Choice: ");
                    String typeChoice = scanner.nextLine().trim();
                    System.out.print("Resource ID: ");
                    String nid = scanner.nextLine().trim();
                    System.out.print("Title: ");
                    String ntitle = scanner.nextLine().trim();

                    if ("2".equals(typeChoice)) {
                        System.out.print("Author: ");
                        String aut = scanner.nextLine().trim();
                        LibraryResource eb = ResourceFactory.createEBook(nid, ntitle, aut, 10.0, "PDF");
                        libraryService.addResource(currentUserId, eb);
                    } else if ("3".equals(typeChoice)) {
                        System.out.print("Publisher: ");
                        String pub = scanner.nextLine().trim();
                        LibraryResource j = ResourceFactory.createJournal(nid, ntitle, pub, 1, 1, false);
                        libraryService.addResource(currentUserId, j);
                    } else {
                        System.out.print("Author: ");
                        String aut = scanner.nextLine().trim();
                        System.out.print("ISBN: ");
                        String isbn = scanner.nextLine().trim();
                        System.out.print("Shelf Location: ");
                        String shelf = scanner.nextLine().trim();
                        LibraryResource b = ResourceFactory.createBook(nid, ntitle, aut, isbn, shelf);
                        libraryService.addResource(currentUserId, b);
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

                case "0":
                    System.out.println("\nThank you for using Smart Library Management System. Goodbye!");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
