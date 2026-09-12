package service;

import factory.LibraryResource;
import model.Loan;
import proxy.LibraryService;
import proxy.LibraryServiceProxy;

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
    public boolean returnResource(String callerUserId, String resourceId) {
        return proxy.returnResource(callerUserId, resourceId);
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
}
