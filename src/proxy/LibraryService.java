package proxy;

import factory.LibraryResource;
import model.Loan;
import model.User;

import java.util.List;

public interface LibraryService {
    boolean borrowResource(String callerUserId, String targetUserId, String resourceId);
    boolean returnResource(String callerUserId, String resourceId);
    boolean reserveResource(String callerUserId, String resourceId);
    LibraryResource searchResource(String query);
    List<LibraryResource> listAllResources();
    List<Loan> getUserLoans(String callerUserId, String targetUserId);
    void addResource(String callerUserId, LibraryResource resource);
}
