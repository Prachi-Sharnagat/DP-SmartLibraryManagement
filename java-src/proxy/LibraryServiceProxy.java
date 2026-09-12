package proxy;

import database.Database;
import factory.LibraryResource;
import model.AuditLogEntry;
import model.BorrowResult;
import model.Loan;
import model.ReturnResult;
import model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Proxy Pattern Implementation for LibraryService.
 * Intercepts calls to provide:
 * 1. Authentication & Role-based Access Control (RBAC)
 * 2. Audit Logging & Persistent Storage
 * 3. Execution time profiling & telemetry
 */
public class LibraryServiceProxy implements LibraryService {
    private final RealLibraryService realService;
    private final Database db;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LibraryServiceProxy() {
        this.realService = new RealLibraryService();
        this.db = Database.getInstance();
    }

    private void logAction(String callerUserId, String action, String details, boolean success, long latencyMs) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.printf("  🛡️  [PROXY AUDIT LOG | %s] Caller: %s | Action: %s | Success: %b | Latency: %dms | Details: %s%n",
                timestamp, callerUserId != null ? callerUserId : "ANONYMOUS", action, success, latencyMs, details);

        String auditId = "AUD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        db.addAuditLog(new AuditLogEntry(auditId, callerUserId, action, details, success, latencyMs));
    }

    private boolean checkAdminOrSelf(String callerUserId, String targetUserId) {
        if (callerUserId == null || callerUserId.trim().isEmpty()) return false;
        if (callerUserId.equalsIgnoreCase(targetUserId)) return true;
        User caller = db.getUser(callerUserId);
        return caller != null && ("ADMIN".equalsIgnoreCase(caller.getRole()) || "LIBRARIAN".equalsIgnoreCase(caller.getRole()) || "STAFF".equalsIgnoreCase(caller.getRole()));
    }

    private boolean checkStaffRole(String callerUserId) {
        if (callerUserId == null || callerUserId.trim().isEmpty()) return false;
        User caller = db.getUser(callerUserId);
        return caller != null && ("ADMIN".equalsIgnoreCase(caller.getRole()) || "LIBRARIAN".equalsIgnoreCase(caller.getRole()) || "STAFF".equalsIgnoreCase(caller.getRole()));
    }

    @Override
    public boolean borrowResource(String callerUserId, String targetUserId, String resourceId) {
        return borrowResourceDetailed(callerUserId, targetUserId, resourceId).isSuccess();
    }

    @Override
    public BorrowResult borrowResourceDetailed(String callerUserId, String targetUserId, String resourceId) {
        long start = System.currentTimeMillis();

        if (!checkAdminOrSelf(callerUserId, targetUserId)) {
            long latency = System.currentTimeMillis() - start;
            String msg = "Access Denied: Cannot borrow items on behalf of other users without staff privileges.";
            logAction(callerUserId, "BORROW_REJECTED", "TargetUser: " + targetUserId + ", Resource: " + resourceId + " | " + msg, false, latency);
            return BorrowResult.fail(msg, "UNAUTHORIZED", "ProxyAccessControl", latency);
        }

        BorrowResult result = realService.borrowResourceDetailed(callerUserId, targetUserId, resourceId);
        long latency = System.currentTimeMillis() - start;
        logAction(callerUserId, "BORROW_EXECUTION", "TargetUser: " + targetUserId + ", Resource: " + resourceId + " | Success: " + result.isSuccess(), result.isSuccess(), latency);
        return result;
    }

    @Override
    public boolean returnResource(String callerUserId, String resourceId) {
        return returnResourceDetailed(callerUserId, resourceId).isSuccess();
    }

    @Override
    public ReturnResult returnResourceDetailed(String callerUserId, String resourceId) {
        long start = System.currentTimeMillis();
        ReturnResult result = realService.returnResourceDetailed(callerUserId, resourceId);
        long latency = System.currentTimeMillis() - start;
        logAction(callerUserId, "RETURN_EXECUTION", "Resource: " + resourceId + " | Fine: $" + result.getFine(), result.isSuccess(), latency);
        return result;
    }

    @Override
    public boolean reserveResource(String callerUserId, String resourceId) {
        long start = System.currentTimeMillis();
        boolean success = realService.reserveResource(callerUserId, resourceId);
        long latency = System.currentTimeMillis() - start;
        logAction(callerUserId, "RESERVE_EXECUTION", "Resource: " + resourceId, success, latency);
        return success;
    }

    @Override
    public LibraryResource searchResource(String query) {
        long start = System.currentTimeMillis();
        LibraryResource res = realService.searchResource(query);
        long latency = System.currentTimeMillis() - start;
        logAction("PUBLIC", "SEARCH", "Query: " + query, res != null, latency);
        return res;
    }

    @Override
    public List<LibraryResource> listAllResources() {
        return realService.listAllResources();
    }

    @Override
    public List<Loan> getUserLoans(String callerUserId, String targetUserId) {
        long start = System.currentTimeMillis();
        if (!checkAdminOrSelf(callerUserId, targetUserId)) {
            logAction(callerUserId, "VIEW_LOANS_DENIED", "TargetUser: " + targetUserId, false, System.currentTimeMillis() - start);
            return List.of();
        }
        List<Loan> loans = realService.getUserLoans(callerUserId, targetUserId);
        logAction(callerUserId, "VIEW_LOANS", "TargetUser: " + targetUserId + " | Count: " + loans.size(), true, System.currentTimeMillis() - start);
        return loans;
    }

    @Override
    public void addResource(String callerUserId, LibraryResource resource) {
        long start = System.currentTimeMillis();
        if (!checkStaffRole(callerUserId)) {
            logAction(callerUserId, "ADD_RESOURCE_DENIED", "Resource: " + (resource != null ? resource.getTitle() : "null"), false, System.currentTimeMillis() - start);
            System.err.println("  [Proxy Security Error] Access Denied: Only ADMIN or LIBRARIAN staff can add new resources.");
            return;
        }
        realService.addResource(callerUserId, resource);
        logAction(callerUserId, "ADD_RESOURCE", "Resource: " + (resource != null ? resource.getTitle() : "null"), true, System.currentTimeMillis() - start);
    }
}
