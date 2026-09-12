package proxy;

import chain.*;
import database.Database;
import factory.LibraryResource;
import model.Loan;
import model.User;
import observer.ReservationManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Concrete Subject / Real Service executing business operations.
 */
public class RealLibraryService implements LibraryService {
    private final Database db;
    private final ValidationHandler validationChain;
    private final ReservationManager reservationManager;

    public RealLibraryService() {
        this.db = Database.getInstance();
        this.reservationManager = ReservationManager.getInstance();

        // Assemble the Chain of Responsibility
        ValidationHandler membershipHandler = new MembershipHandler();
        ValidationHandler fineHandler = new FineHandler();
        ValidationHandler limitHandler = new BorrowLimitHandler();
        ValidationHandler availabilityHandler = new AvailabilityHandler();
        ValidationHandler reservationHandler = new ReservationHandler();

        membershipHandler
                .setNext(fineHandler)
                .setNext(limitHandler)
                .setNext(availabilityHandler)
                .setNext(reservationHandler);

        this.validationChain = membershipHandler;
    }

    @Override
    public boolean borrowResource(String callerUserId, String targetUserId, String resourceId) {
        User user = db.getUser(targetUserId);
        LibraryResource resource = db.getResource(resourceId);

        System.out.println("  [RealLibraryService] Initiating validation pipeline for borrow request...");
        ValidationHandler.ValidationResult result = validationChain.validate(user, resource);

        if (!result.isValid()) {
            System.err.println("  [RealLibraryService] Borrow Rejected: " + result.getMessage());
            return false;
        }

        // Process Borrow
        resource.setAvailable(false);
        user.setCurrentBorrowedCount(user.getCurrentBorrowedCount() + 1);

        String loanId = "LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDate now = LocalDate.now();
        LocalDate dueDate = now.plusDays(resource.getMaxBorrowDays());

        Loan loan = new Loan(loanId, user.getUserId(), resource.getId(), now, dueDate);
        db.saveLoan(loan);
        db.saveUser(user);
        db.saveResource(resource);

        // If reserved, consume reservation
        ReservationHandler.removeReservation(resource.getId());

        System.out.printf("  [RealLibraryService] SUCCESS: '%s' borrowed by '%s' (Due Date: %s, Loan ID: %s)%n",
                resource.getTitle(), user.getName(), dueDate, loanId);
        return true;
    }

    @Override
    public boolean returnResource(String callerUserId, String resourceId) {
        LibraryResource resource = db.getResource(resourceId);
        if (resource == null) {
            System.err.println("  [RealLibraryService] Error: Resource not found.");
            return false;
        }

        Loan activeLoan = db.getActiveLoanForResource(resourceId);
        if (activeLoan == null) {
            System.err.println("  [RealLibraryService] Warning: No active loan record found for resource ID: " + resourceId);
        } else {
            activeLoan.setReturned(true);
            activeLoan.setReturnDate(LocalDate.now());
            db.saveLoan(activeLoan);

            User borrower = db.getUser(activeLoan.getUserId());
            if (borrower != null && borrower.getCurrentBorrowedCount() > 0) {
                borrower.setCurrentBorrowedCount(borrower.getCurrentBorrowedCount() - 1);
                db.saveUser(borrower);
            }
        }

        resource.setAvailable(true);
        db.saveResource(resource);
        System.out.printf("  [RealLibraryService] SUCCESS: '%s' (ID: %s) has been returned into library inventory.%n",
                resource.getTitle(), resource.getId());

        // Notify Observers via ReservationManager
        reservationManager.notifyObservers(
                resource.getId(),
                resource.getTitle(),
                "RESOURCE_AVAILABLE",
                "Item is ready for pickup or checkout."
        );

        return true;
    }

    @Override
    public boolean reserveResource(String callerUserId, String resourceId) {
        LibraryResource resource = db.getResource(resourceId);
        if (resource == null) {
            System.err.println("  [RealLibraryService] Resource not found.");
            return false;
        }

        ReservationHandler.setReservation(resourceId, callerUserId);
        System.out.printf("  [RealLibraryService] Reservation recorded: Resource '%s' is now held for User ID: %s%n",
                resource.getTitle(), callerUserId);
        return true;
    }

    @Override
    public LibraryResource searchResource(String query) {
        if (query == null || query.trim().isEmpty()) return null;
        String q = query.toLowerCase().trim();
        for (LibraryResource r : db.getAllResources()) {
            if (r.getId().equalsIgnoreCase(q) || r.getTitle().toLowerCase().contains(q)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public List<LibraryResource> listAllResources() {
        return db.getAllResources();
    }

    @Override
    public List<Loan> getUserLoans(String callerUserId, String targetUserId) {
        return db.getActiveLoansByUser(targetUserId);
    }

    @Override
    public void addResource(String callerUserId, LibraryResource resource) {
        db.saveResource(resource);
        System.out.printf("  [RealLibraryService] Added new resource: '%s' (ID: %s)%n", resource.getTitle(), resource.getId());
    }
}
