package template;

import chain.*;
import factory.LibraryResource;
import model.AuditLogEntry;
import model.BorrowResult;
import model.Loan;
import model.User;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Concrete Template Method implementation for Book/Resource Checkout workflow.
 * Executes validation via Chain of Responsibility, enforces atomic state transitions,
 * and maintains rollback safety in case of database or system failure.
 */
public class IssueBookTransaction extends LibraryTransaction {
    private final String targetUserId;
    private final String resourceId;
    private final ValidationHandler validationChain;

    // Entity state
    private User user;
    private LibraryResource resource;
    private Loan createdLoan;

    // Snapshot state for atomic rollback
    private boolean previousAvailability = true;
    private int previousBorrowedCount = 0;
    private boolean executedMutation = false;

    public IssueBookTransaction(String callerUserId, String targetUserId, String resourceId) {
        super("ISSUE_BOOK", callerUserId);
        this.targetUserId = targetUserId;
        this.resourceId = resourceId;

        // Build 5-step Chain of Responsibility validation pipeline
        ValidationHandler r1 = new MembershipHandler();
        ValidationHandler r2 = new FineHandler();
        ValidationHandler r3 = new BorrowLimitHandler();
        ValidationHandler r4 = new AvailabilityHandler();
        ValidationHandler r5 = new ReservationHandler();

        r1.setNext(r2);
        r2.setNext(r3);
        r3.setNext(r4);
        r4.setNext(r5);
        this.validationChain = r1;
    }

    @Override
    protected void validate() throws TransactionException {
        user = db.getUser(targetUserId);
        if (user == null) {
            throw new TransactionException("Member not found with ID: " + targetUserId, "MEMBER_NOT_FOUND");
        }

        resource = db.getResource(resourceId);
        if (resource == null) {
            throw new TransactionException("Library resource not found with ID: " + resourceId, "RESOURCE_NOT_FOUND");
        }

        ValidationHandler.ValidationResult valResult = validationChain.validate(user, resource);
        if (!valResult.isValid()) {
            throw new TransactionException(valResult.getMessage(), mapHandlerToCode(valResult.getFailedHandlerName()));
        }
    }

    private String mapHandlerToCode(String handlerName) {
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
    protected void prepare() throws TransactionException {
        previousAvailability = resource.isAvailable();
        previousBorrowedCount = user.getCurrentBorrowedCount();

        String loanId = "LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDate now = LocalDate.now();
        LocalDate dueDate = now.plusDays(resource.getMaxBorrowDays());

        this.createdLoan = new Loan(loanId, user.getUserId(), resource.getId(), now, dueDate);
        logStep("Prepared Loan " + loanId + " with due date: " + dueDate);
    }

    @Override
    protected void executeTransaction() throws TransactionException {
        resource.setAvailable(false);
        user.setCurrentBorrowedCount(user.getCurrentBorrowedCount() + 1);
        executedMutation = true;
        logStep("Updated resource availability to false and incremented user loan count to " + user.getCurrentBorrowedCount());
    }

    @Override
    protected void persist() throws Exception {
        db.saveLoan(createdLoan);
        db.saveUser(user);
        db.saveResource(resource);

        // Consume reservation if this user was the reservation holder
        ReservationHandler.removeReservation(resource.getId());
        db.removeReservation(resource.getId());
        logStep("Persisted Loan, User, and Resource to database storage");
    }

    @Override
    protected void rollback() {
        if (executedMutation) {
            if (resource != null) {
                resource.setAvailable(previousAvailability);
                db.saveResource(resource);
            }
            if (user != null) {
                user.setCurrentBorrowedCount(previousBorrowedCount);
                db.saveUser(user);
            }
            logStep("Rollback executed: restored resource and user states");
        } else {
            logStep("Rollback executed: no state changes required");
        }
    }

    @Override
    protected void recordAuditLog(boolean success, String errorDetails) {
        long latency = System.currentTimeMillis() - startTime;
        String details = String.format("TargetUser: %s, Resource: %s | Success: %b%s",
                targetUserId, resourceId, success, (errorDetails != null ? " (" + errorDetails + ")" : ""));

        db.addAuditLog(new AuditLogEntry(
                "AUD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                callerUserId,
                "BORROW_EXECUTION",
                details,
                success,
                latency
        ));
    }

    @Override
    protected TransactionResult generateSuccessResult() {
        long latency = System.currentTimeMillis() - startTime;
        String msg = String.format("Resource '%s' issued successfully. Due Date: %s",
                resource.getTitle(), createdLoan.getDueDate());
        BorrowResult borrowResult = BorrowResult.ok(msg, createdLoan, latency);
        return new TransactionResult(true, transactionId, transactionType, msg, null, executionSteps, latency, borrowResult);
    }

    @Override
    protected TransactionResult generateFailureResult(String message, String errorCode) {
        long latency = System.currentTimeMillis() - startTime;
        BorrowResult borrowResult = BorrowResult.fail(message, errorCode, "PipelineValidation", latency);
        return new TransactionResult(false, transactionId, transactionType, message, errorCode, executionSteps, latency, borrowResult);
    }

    public Loan getCreatedLoan() { return createdLoan; }
}
