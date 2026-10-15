package service;

import factory.LibraryResource;
import memento.ResourceMemento;
import model.BorrowResult;
import model.Loan;
import model.ReturnResult;
import proxy.LibraryService;
import proxy.LibraryServiceProxy;
import strategy.FineCalculationResult;

import java.util.List;

/**
 * Service facade providing unified interface to the application.
 */
public class LibraryServiceImpl implements LibraryService {
    private final LibraryService proxy;

    public LibraryServiceImpl() {
        this.proxy = new LibraryServiceProxy();
    }

    @Override
    public boolean borrowResource(String callerUserId, String targetUserId, String resourceId) {
        return proxy.borrowResource(callerUserId, targetUserId, resourceId);
    }

    @Override
    public BorrowResult borrowResourceDetailed(String callerUserId, String targetUserId, String resourceId) {
        return proxy.borrowResourceDetailed(callerUserId, targetUserId, resourceId);
    }

    @Override
    public boolean returnResource(String callerUserId, String resourceId) {
        return proxy.returnResource(callerUserId, resourceId);
    }

    @Override
    public ReturnResult returnResourceDetailed(String callerUserId, String resourceId) {
        return proxy.returnResourceDetailed(callerUserId, resourceId);
    }

    @Override
    public boolean reserveResource(String callerUserId, String resourceId) {
        return proxy.reserveResource(callerUserId, resourceId);
    }

    @Override
    public LibraryResource searchResource(String query) {
        return proxy.searchResource(query);
    }

    @Override
    public List<LibraryResource> listAllResources() {
        return proxy.listAllResources();
    }

    @Override
    public List<Loan> getUserLoans(String callerUserId, String targetUserId) {
        return proxy.getUserLoans(callerUserId, targetUserId);
    }

    @Override
    public void addResource(String callerUserId, LibraryResource resource) {
        proxy.addResource(callerUserId, resource);
    }

    @Override
    public LibraryResource createBookCopy(String callerUserId, String sourceResourceId, String newCopyId, String newShelfLocation) {
        return proxy.createBookCopy(callerUserId, sourceResourceId, newCopyId, newShelfLocation);
    }

    @Override
    public boolean updateResourceMetadata(String callerUserId, String resourceId, String newTitle,
                                          String newAuthor, String newShelf, String newCategory,
                                          String newPublisher, String newEdition, int newYear) {
        return proxy.updateResourceMetadata(callerUserId, resourceId, newTitle, newAuthor, newShelf, newCategory, newPublisher, newEdition, newYear);
    }

    @Override
    public boolean undoLastEdit(String callerUserId) {
        return proxy.undoLastEdit(callerUserId);
    }

    @Override
    public List<ResourceMemento> getUndoHistory(String callerUserId) {
        return proxy.getUndoHistory(callerUserId);
    }

    @Override
    public FineCalculationResult previewFine(String userId, String resourceId, int overdueDays) {
        return proxy.previewFine(userId, resourceId, overdueDays);
    }
}
