package proxy;

import chain.ReservationHandler;
import database.Database;
import factory.Book;
import factory.LibraryResource;
import mediator.LibraryMediator;
import mediator.LibraryMediatorImpl;
import memento.LibraryHistory;
import memento.ResourceMemento;
import model.BorrowResult;
import model.Loan;
import model.ReturnResult;
import model.User;
import observer.ReservationManager;
import strategy.FineCalculationResult;
import strategy.FineCalculationStrategy;
import strategy.FineStrategyFactory;
import template.IssueBookTransaction;
import template.LibraryTransaction;
import template.ReturnBookTransaction;
import template.TransactionResult;

import java.time.LocalDate;
import java.util.List;

/**
 * Concrete Subject / Real Service executing library business logic.
 * Integrates:
 * - Template Method: IssueBookTransaction & ReturnBookTransaction
 * - Mediator: LibraryMediator coordinating return circulation
 * - Strategy: FineStrategyFactory for dynamic role-based fine calculations
 * - Prototype: createBookCopy cloning metadata into independent records
 * - Memento: updateResourceMetadata & undoLastEdit safe compensating rollback
 */
public class RealLibraryService implements LibraryService {
    private final Database db;
    private final ReservationManager reservationManager;
    private final LibraryMediator mediator;
    private final LibraryHistory history;

    public RealLibraryService() {
        this.db = Database.getInstance();
        this.reservationManager = ReservationManager.getInstance();
        this.mediator = LibraryMediatorImpl.getInstance();
        this.history = LibraryHistory.getInstance();
    }

    @Override
    public boolean borrowResource(String callerUserId, String targetUserId, String resourceId) {
        BorrowResult res = borrowResourceDetailed(callerUserId, targetUserId, resourceId);
        return res.isSuccess();
    }

    /**
     * Executes Book Checkout through the Template Method pattern (IssueBookTransaction).
     * Enforces sequential validation, preparation, execution, persistence, and audit logging.
     */
    @Override
    public BorrowResult borrowResourceDetailed(String callerUserId, String targetUserId, String resourceId) {
        LibraryTransaction transaction = new IssueBookTransaction(callerUserId, targetUserId, resourceId);
        TransactionResult txResult = transaction.execute();
        return (BorrowResult) txResult.getPayload();
    }

    @Override
    public boolean returnResource(String callerUserId, String resourceId) {
        ReturnResult res = returnResourceDetailed(callerUserId, resourceId);
        return res.isSuccess();
    }

    /**
     * Executes Book Return through the Template Method pattern (ReturnBookTransaction),
     * which delegates cross-service coordination to the LibraryMediator.
     */
    @Override
    public ReturnResult returnResourceDetailed(String callerUserId, String resourceId) {
        LibraryTransaction transaction = new ReturnBookTransaction(callerUserId, resourceId, mediator);
        TransactionResult txResult = transaction.execute();
        return (ReturnResult) txResult.getPayload();
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

    // ==========================================
    // PROTOTYPE DESIGN PATTERN INTEGRATION
    // ==========================================

    @Override
    public LibraryResource createBookCopy(String callerUserId, String sourceResourceId, String newCopyId, String newShelfLocation) {
        if (sourceResourceId == null || newCopyId == null) {
            throw new IllegalArgumentException("Source Resource ID and New Copy ID are required.");
        }

        LibraryResource source = db.getResource(sourceResourceId);
        if (source == null) {
            throw new IllegalArgumentException("Source resource not found with ID: " + sourceResourceId);
        }

        if (!(source instanceof Book)) {
            throw new UnsupportedOperationException("Prototype copy cloning is supported for physical Book catalog resources.");
        }

        if (db.getResource(newCopyId.trim()) != null) {
            throw new IllegalStateException("Cannot create copy: A resource with ID '" + newCopyId.trim() + "' already exists in database.");
        }

        Book sourceBook = (Book) source;
        Book copy = sourceBook.createCopy(newCopyId.trim(), newShelfLocation);
        db.saveResource(copy);

        System.out.printf("  [RealLibraryService | Prototype] Cloned new physical copy '%s' (Copy ID: %s, Shelf: %s) from source '%s'%n",
                copy.getTitle(), copy.getId(), copy.getShelfLocation(), sourceResourceId);

        return copy;
    }

    // ==========================================
    // MEMENTO DESIGN PATTERN INTEGRATION
    // ==========================================

    @Override
    public boolean updateResourceMetadata(String callerUserId, String resourceId, String newTitle,
                                          String newAuthor, String newShelf, String newCategory,
                                          String newPublisher, String newEdition, int newYear) {
        LibraryResource res = db.getResource(resourceId);
        if (res == null || !(res instanceof Book)) {
            return false;
        }

        Book book = (Book) res;

        // Capture snapshot into Caretaker before mutation
        ResourceMemento snapshot = book.createMemento(String.format("Pre-update snapshot by %s", callerUserId));
        history.pushSnapshot(snapshot);

        // Mutate fields
        if (newTitle != null && !newTitle.trim().isEmpty()) book.setTitle(newTitle.trim());
        if (newAuthor != null && !newAuthor.trim().isEmpty()) book.setAuthor(newAuthor.trim());
        if (newShelf != null && !newShelf.trim().isEmpty()) book.setShelfLocation(newShelf.trim());
        if (newCategory != null && !newCategory.trim().isEmpty()) book.setCategory(newCategory.trim());
        if (newPublisher != null && !newPublisher.trim().isEmpty()) book.setPublisher(newPublisher.trim());
        if (newEdition != null && !newEdition.trim().isEmpty()) book.setEdition(newEdition.trim());
        if (newYear > 0) book.setPublicationYear(newYear);

        // Safe database update
        db.saveResource(book);
        System.out.printf("  [RealLibraryService | Memento] Saved metadata update for '%s' (ID: %s). Snapshot pushed to history.%n",
                book.getTitle(), resourceId);
        return true;
    }

    @Override
    public boolean undoLastEdit(String callerUserId) {
        ResourceMemento restored = history.undo(db, callerUserId);
        if (restored != null) {
            System.out.printf("  [RealLibraryService | Memento] Successfully reverted resource %s to snapshot [%s]%n",
                    restored.getResourceId(), restored.getDescription());
            return true;
        }
        return false;
    }

    @Override
    public List<ResourceMemento> getUndoHistory(String callerUserId) {
        return history.getUndoHistory();
    }

    // ==========================================
    // STRATEGY DESIGN PATTERN INTEGRATION
    // ==========================================

    @Override
    public FineCalculationResult previewFine(String userId, String resourceId, int overdueDays) {
        User user = db.getUser(userId);
        FineCalculationStrategy strategy = FineStrategyFactory.getStrategy(user);
        LocalDate dueDate = LocalDate.now().minusDays(Math.max(0, overdueDays));
        LocalDate returnDate = LocalDate.now();
        return strategy.calculateFine(dueDate, returnDate);
    }
}
