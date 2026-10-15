package mediator;

import database.Database;
import factory.LibraryResource;
import model.Loan;
import model.User;

import java.time.LocalDate;

/**
 * Colleague handling loan status transitions and resource availability.
 * Has no direct knowledge of Notification or Fine services; delegates coordination through Mediator.
 */
public class CirculationColleague extends Colleague {
    private final Database db;

    public CirculationColleague(Database db) {
        this.db = db;
    }

    public Loan getActiveLoanForResource(String resourceId) {
        return db.getActiveLoanForResource(resourceId);
    }

    public LibraryResource getResource(String resourceId) {
        return db.getResource(resourceId);
    }

    public User getUser(String userId) {
        return db.getUser(userId);
    }

    public void completeLoanReturn(Loan loan, LocalDate returnDate, double fineAmount) {
        if (loan == null) return;
        loan.setReturned(true);
        loan.setReturnDate(returnDate);
        loan.setFine(fineAmount);
        db.saveLoan(loan);

        // Decrement user borrowed count
        User borrower = db.getUser(loan.getUserId());
        if (borrower != null) {
            borrower.setCurrentBorrowedCount(Math.max(0, borrower.getCurrentBorrowedCount() - 1));
            db.saveUser(borrower);
        }
    }

    public void updateResourceAvailability(LibraryResource resource, boolean available) {
        if (resource == null) return;
        resource.setAvailable(available);
        db.saveResource(resource);
    }
}
