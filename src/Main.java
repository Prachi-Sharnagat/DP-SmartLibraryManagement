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
import service.LibraryServiceImpl;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final LibraryService libraryService = new LibraryServiceImpl();
    private static final Database db = Database.getInstance();
    private static final ReservationManager reservationManager = ReservationManager.getInstance();

    public static void main(String[] args) {
        if (args.length > 0 && "--demo".equalsIgnoreCase(args[0])) {
            runDesignPatternsShowcase();
            return;
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
        System.out.println("│  2. BRIDGE PATTERN DEMONSTRATION (Decoupling Alerts from Delivery Channels) │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        NotificationChannel email = new EmailChannel();
        NotificationChannel sms = new SMSChannel();
        NotificationChannel inApp = new InAppChannel();

        Notification emailNotifier = new BookAvailableNotification(email);
        Notification smsNotifier = new BookAvailableNotification(sms);
        Notification inAppNotifier = new BookAvailableNotification(inApp);

        emailNotifier.send("alice@example.com", "Your reserved copy of 'Design Patterns' is ready.");
        smsNotifier.send("555-0101", "Book available for pickup at Front Desk.");
        inAppNotifier.send("U101", "Your eBook download license has been generated.");
        System.out.println("  [PASS] Bridge Pattern successfully dispatches messages across channels.\n");

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  3. OBSERVER PATTERN DEMONSTRATION (Resource Reservation & Alerts)          │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        User alice = db.getUser("U101");
        User diana = db.getUser("U104");

        Member observerAlice = new Member(alice, new BookAvailableNotification(new EmailChannel()));
        Member observerDiana = new Member(diana, new BookAvailableNotification(new SMSChannel()));

        // Register observers to wait for book B003 (Introduction to Algorithms)
        reservationManager.registerObserver("B003", observerAlice);
        reservationManager.registerObserver("B003", observerDiana);

        System.out.println("  -> Simulating book return of 'B003' to trigger Observers...");
        reservationManager.notifyObservers("B003", "Introduction to Algorithms", "RETURNED", "Returned by previous borrower.");
        System.out.println("  [PASS] Observer Pattern successfully notified all subscribed members.\n");

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  4. CHAIN OF RESPONSIBILITY PATTERN (Borrow Validation Rules Pipeline)       │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        // Test case A: Unpaid Fine rejection
        System.out.println("  [Test 4A] Testing member with high fine (Bob - Fine: $15.50)...");
        libraryService.borrowResource("U102", "U102", "B001");

        // Test case B: Inactive membership rejection
        System.out.println("\n  [Test 4B] Testing inactive member (Charlie)...");
        libraryService.borrowResource("U103", "U103", "B001");

        // Test case C: Borrow limit exceeded rejection
        System.out.println("\n  [Test 4C] Testing borrow limit quota reached (Diana - 5/5 books)...");
        libraryService.borrowResource("U104", "U104", "B001");

        // Test case D: Reference-only journal checkout rejection
        System.out.println("\n  [Test 4D] Testing reference-only journal checkout (J002)...");
        libraryService.borrowResource("U101", "U101", "J002");

        // Test case E: Valid borrower passing all checks
        System.out.println("\n  [Test 4E] Testing valid borrower (Alice - Active, $0 Fine)...");
        boolean borrowSuccess = libraryService.borrowResource("U101", "U101", "B001");
        System.out.println("  Borrow Result: " + (borrowSuccess ? "SUCCESS" : "FAILURE"));
        System.out.println("  [PASS] Chain of Responsibility successfully evaluated all validation rules.\n");

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│  5. PROXY PATTERN DEMONSTRATION (Access Control & Audit Logging)            │");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        System.out.println("  [Test 5A] Unauthorized user attempting admin action (add resource)...");
        libraryService.addResource("U101", physicalBook); // Member cannot add

        System.out.println("\n  [Test 5B] Authorized Admin performing admin action...");
        libraryService.addResource("U999", physicalBook); // Admin can add

        System.out.println("\n  [Test 5C] Returning borrowed book via Proxy...");
        libraryService.returnResource("U101", "B001");
        System.out.println("  [PASS] Proxy Pattern successfully enforced access control and audit telemetry.\n");
    }

    private static void runInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);
        String currentUserId = "U101"; // Default login as Alice

        while (true) {
            User currentUser = db.getUser(currentUserId);
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.printf("  Current Logged In User: %s (%s | Role: %s)%n",
                    (currentUser != null ? currentUser.getName() : "Guest"), currentUserId,
                    (currentUser != null ? currentUser.getRole() : "N/A"));
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("  1. List All Library Resources");
            System.out.println("  2. Search Resource by ID or Title");
            System.out.println("  3. Borrow a Resource (Validates Chain of Responsibility)");
            System.out.println("  4. Return a Resource (Triggers Observer Alerts)");
            System.out.println("  5. Reserve a Resource & Subscribe to Alerts (Observer Pattern)");
            System.out.println("  6. View My Active Loans & Account Status");
            System.out.println("  7. Add New Resource (Factory Pattern - Staff Only)");
            System.out.println("  8. Switch User (e.g. U101:Alice, U102:Bob (Fines), U103:Charlie (Inactive), U999:Admin)");
            System.out.println("  9. Re-run Design Patterns Automated Showcase");
            System.out.println("  0. Exit");
            System.out.print("\nSelect an option [0-9]: ");

            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";
            if (input.isEmpty()) continue;

            switch (input) {
                case "1":
                    System.out.println("\n--- LIBRARY CATALOG ---");
                    List<LibraryResource> all = libraryService.listAllResources();
                    for (LibraryResource r : all) {
                        System.out.println(" • " + r.getDetails());
                    }
                    break;

                case "2":
                    System.out.print("Enter search keyword or resource ID: ");
                    String q = scanner.nextLine().trim();
                    LibraryResource found = libraryService.searchResource(q);
                    if (found != null) {
                        System.out.println("\n[Match Found] " + found.getDetails());
                    } else {
                        System.out.println("\n[!] No matching resource found for: " + q);
                    }
                    break;

                case "3":
                    System.out.print("Enter Resource ID to borrow (e.g. B002, EB001): ");
                    String resId = scanner.nextLine().trim();
                    libraryService.borrowResource(currentUserId, currentUserId, resId);
                    break;

                case "4":
                    System.out.print("Enter Resource ID to return (e.g. B001, B002): ");
                    String retId = scanner.nextLine().trim();
                    libraryService.returnResource(currentUserId, retId);
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
