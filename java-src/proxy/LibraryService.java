package proxy;

import factory.LibraryResource;
import memento.ResourceMemento;
import model.BorrowResult;
import model.Loan;
import model.ReturnResult;
import strategy.FineCalculationResult;

import java.util.List;

public interface LibraryService {
    // Circulation Transactions (Template Method & Chain of Responsibility)
    boolean borrowResource(String callerUserId, String targetUserId, String resourceId);
    BorrowResult borrowResourceDetailed(String callerUserId, String targetUserId, String resourceId);
    
    // Return Transactions (Template Method & Mediator)
    boolean returnResource(String callerUserId, String resourceId);
    ReturnResult returnResourceDetailed(String callerUserId, String resourceId);
    
    // Reservation Management (Observer & Bridge)
    boolean reserveResource(String callerUserId, String resourceId);
    LibraryResource searchResource(String query);
    List<LibraryResource> listAllResources();
    List<Loan> getUserLoans(String callerUserId, String targetUserId);
    void addResource(String callerUserId, LibraryResource resource);

    // Prototype Pattern: Clone Book Copies & Templates
    LibraryResource createBookCopy(String callerUserId, String sourceResourceId, String newCopyId, String newShelfLocation);

    // Memento Pattern: Edit & Compensating Rollback
    boolean updateResourceMetadata(String callerUserId, String resourceId, String newTitle, String newAuthor, String newShelf, String newCategory, String newPublisher, String newEdition, int newYear);
    boolean undoLastEdit(String callerUserId);
    List<ResourceMemento> getUndoHistory(String callerUserId);

    // Strategy Pattern: Fine Policy Calculation & Preview
    FineCalculationResult previewFine(String userId, String resourceId, int overdueDays);
}
