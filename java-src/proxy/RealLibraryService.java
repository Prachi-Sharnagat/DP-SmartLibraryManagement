package proxy;

import chain.*;
import database.Database;
import factory.LibraryResource;
import model.BorrowResult;
import model.Loan;
import model.ReturnResult;
import model.User;
import observer.Observer;
import observer.ReservationManager;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Concrete Subject / Real Service executing core library business logic.
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
        BorrowResult res = borrowResourceDetailed(callerUserId, targetUserId, resourceId);
        return res.isSuccess();
    }

    @Override
    public BorrowResult borrowResourceDetailed(String callerUserId, String targetUserId, String resourceId) {
        long start = System.currentTimeMillis();
        User user = db.getUser(targetUserId);
        if (user == null) {
            return BorrowResult.fail("Member not found with ID: " + targetUserId, "MEMBER_NOT_FOUND", "UserLookup", System.currentTimeMillis() - start);
        }

        LibraryResource resource = db.getResource(resourceId);
        if (resource == null) {
            return BorrowResult.fail("Library resource not found with ID: " + resourceId, "RESOURCE_NOT_FOUND", "ResourceLookup", System.currentTimeMillis() - start);
        }

        System.out.println("  [RealLibraryService] Initiating validation pipeline for borrow request...");
        ValidationHandler.ValidationResult result = validationChain.validate(user, resource);

        if (!result.isValid()) {
            System.err.println("  [RealLibraryService] Borrow Rejected: " + result.getMessage());
            String errCode = mapFailedHandlerToErrorCode(result.getFailedHandlerName());
            return BorrowResult.fail(result.getMessage(), errCode, result.getFailedHandlerName(), System.currentTimeMillis() - start);
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
        db.removeReservation(resource.getId());

        long latency = System.currentTimeMillis() - start;
        System.out.printf("  [RealLibraryService] SUCCESS: '%s' borrowed by '%s' (Due Date: %s, Loan ID: %s, %dms)%n",
                resource.getTitle(), user.getName(), dueDate, loanId, latency);

        return BorrowResult.ok("Resource issued successfully. Due date: " + dueDate, loan, latency);
    }

    private String mapFailedHandlerToErrorCode(String handlerName) {
        if (handlerName == null) return "VALIDATION_FAILED";
        switch (handlerName) {
            case "MembershipHandler": return "MEMBERSHIP_INACTIVE";
            case "FineHandler": return "FINE_LIMIT_EXCEEDED";
            case "BorrowLimitHandler": return "BORROW_LIMIT_EXCEEDED";
            case "AvailabilityHandler": return "RESOURCE_UNAVAILABLE";
            case "ReservationHandler": return "RESERVED_BY_OTHER";
            default: return "VALIDATION_FAILED";
        }
    }

    @Override
    public boolean returnResource(String callerUserId, String resourceId) {
        ReturnResult res = returnResourceDetailed(callerUserId, resourceId);
        return res.isSuccess();
    }

    @Override
    public ReturnResult returnResourceDetailed(String callerUserId, String resourceId) {
        long start = System.currentTimeMillis();
        LibraryResource resource = db.getResource(resourceId);
        if (resource == null) {
            return new ReturnResult(false, "Resource not found with ID: " + resourceId, 0, 0, 0, System.currentTimeMillis() - start);
        }

        Loan activeLoan = db.getActiveLoanForResource(resourceId);
        double fine = 0.0;
        int daysOverdue = 0;

        if (activeLoan != null) {
            LocalDate today = LocalDate.now();
            activeLoan.setReturned(true);
            activeLoan.setReturnDate(today);

            if (activeLoan.getDueDate() != null && today.isAfter(activeLoan.getDueDate())) {
                daysOverdue = (int) ChronoUnit.DAYS.between(activeLoan.getDueDate(), today);
                fine = daysOverdue * 0.50; // $0.50 per overdue day
                activeLoan.setFine(fine);
            }

            db.saveLoan(activeLoan);

            User borrower = db.getUser(activeLoan.getUserId());
            if (borrower != null) {
                if (borrower.getCurrentBorrowedCount() > 0) {
                    borrower.setCurrentBorrowedCount(borrower.getCurrentBorrowedCount() - 1);
                }
                if (fine > 0) {
                    borrower.setOutstandingFine(borrower.getOutstandingFine() + fine);
                }
                db.saveUser(borrower);
            }
        }

        resource.setAvailable(true);
        db.saveResource(resource);

        // Notify Observers via ReservationManager
        List<Observer> waitlist = reservationManager.getWaitlist(resource.getId());
        int notifiedCount = waitlist.size();

        reservationManager.notifyObservers(
                resource.getId(),
                resource.getTitle(),
                "RESOURCE_AVAILABLE",
                "Item '" + resource.getTitle() + "' is now returned and available for checkout."
        );

        long latency = System.currentTimeMillis() - start;
        String msg = fine > 0
                ? String.format("Resource returned with %d overdue day(s). Fine incurred: $%.2f. %d observer(s) notified.", daysOverdue, fine, notifiedCount)
                : String.format("Resource returned on time. Inventory updated. %d observer(s) notified.", notifiedCount);

        return new ReturnResult(true, msg, fine, daysOverdue, notifiedCount, latency);
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
