package proxy;

import database.Database;
import factory.LibraryResource;
import model.Loan;
import model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Proxy Pattern Implementation for LibraryService.
 * Intercepts calls to provide:
 * 1. Authentication & Role-based Access Control (RBAC)
 * 2. Audit Logging
 * 3. Execution time profiling
 */
public class LibraryServiceProxy implements LibraryService {
    private final RealLibraryService realService;
    private final Database db;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LibraryServiceProxy() {
        this.realService = new RealLibraryService();
        this.db = Database.getInstance();
    }

    private void logAction(String callerUserId, String action, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.printf("  🛡️  [PROXY AUDIT LOG | %s] Caller: %s | Action: %s | Details: %s%n",
                timestamp, callerUserId != null ? callerUserId : "ANONYMOUS", action, details);
    }

    private boolean checkAdminOrSelf(String callerUserId, String targetUserId) {
        if (callerUserId == null) return false;
        if (callerUserId.equalsIgnoreCase(targetUserId)) return true;
        User caller = db.getUser(callerUserId);
        return caller != null && ("ADMIN".equalsIgnoreCase(caller.getRole()) || "LIBRARIAN".equalsIgnoreCase(caller.getRole()));
    }

    private boolean checkStaffRole(String callerUserId) {
        if (callerUserId == null) return false;
        User caller = db.getUser(callerUserId);
        return caller != null && ("ADMIN".equalsIgnoreCase(caller.getRole()) || "LIBRARIAN".equalsIgnoreCase(caller.getRole()));
    }

    @Override
    public boolean borrowResource(String callerUserId, String targetUserId, String resourceId) {
        long start = System.currentTimeMillis();
        logAction(callerUserId, "BORROW_REQUEST", "TargetUser: " + targetUserId + ", Resource: " + resourceId);

        if (!checkAdminOrSelf(callerUserId, targetUserId)) {
            System.err.println("  [Proxy Security Error] Access Denied: Cannot borrow items on behalf of other users without staff privileges.");
            return false;
        }

        boolean success = realService.borrowResource(callerUserId, targetUserId, resourceId);
        long duration = System.currentTimeMillis() - start;
        logAction(callerUserId, "BORROW_COMPLETED", "Success: " + success + " | Latency: " + duration + "ms");
        return success;
    }

    @Override
    public boolean returnResource(String callerUserId, String resourceId) {
        long start = System.currentTimeMillis();
        logAction(callerUserId, "RETURN_REQUEST", "Resource: " + resourceId);
        boolean success = realService.returnResource(callerUserId, resourceId);
        long duration = System.currentTimeMillis() - start;
        logAction(callerUserId, "RETURN_COMPLETED", "Success: " + success + " | Latency: " + duration + "ms");
        return success;
    }

    @Override
    public boolean reserveResource(String callerUserId, String resourceId) {
        logAction(callerUserId, "RESERVE_REQUEST", "Resource: " + resourceId);
        return realService.reserveResource(callerUserId, resourceId);
    }

    @Override
    public LibraryResource searchResource(String query) {
        logAction("PUBLIC", "SEARCH", "Query: " + query);
        return realService.searchResource(query);
    }

    @Override
    public List<LibraryResource> listAllResources() {
        return realService.listAllResources();
    }

    @Override
    public List<Loan> getUserLoans(String callerUserId, String targetUserId) {
        logAction(callerUserId, "VIEW_LOANS", "TargetUser: " + targetUserId);
        if (!checkAdminOrSelf(callerUserId, targetUserId)) {
            System.err.println("  [Proxy Security Error] Access Denied: Cannot view loan records of another member.");
            return List.of();
        }
        return realService.getUserLoans(callerUserId, targetUserId);
    }

    @Override
    public void addResource(String callerUserId, LibraryResource resource) {
        logAction(callerUserId, "ADD_RESOURCE", "Resource: " + (resource != null ? resource.getTitle() : "null"));
        if (!checkStaffRole(callerUserId)) {
            System.err.println("  [Proxy Security Error] Access Denied: Only ADMIN or LIBRARIAN staff can add new resources.");
            return;
        }
        realService.addResource(callerUserId, resource);
    }
}
