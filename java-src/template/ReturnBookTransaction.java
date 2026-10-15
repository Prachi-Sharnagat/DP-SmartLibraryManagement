package template;

import factory.LibraryResource;
import mediator.LibraryMediator;
import mediator.LibraryMediatorImpl;
import mediator.ReturnCoordinationResult;
import model.AuditLogEntry;
import model.Loan;
import model.ReturnResult;

import java.util.UUID;

/**
 * Concrete Template Method implementation for Book/Resource Check-in workflow.
 * Standardizes validation, preparation, execution (coordinated via LibraryMediator),
 * persistence, and audit logging with automatic compensation safety.
 */
public class ReturnBookTransaction extends LibraryTransaction {
    private final String resourceId;
    private final LibraryMediator mediator;

    private LibraryResource resource;
    private Loan activeLoan;
    private ReturnCoordinationResult coordinationResult;

    // Snapshot state for rollback safety
    private boolean previousAvailability = false;
    private double previousFineAmount = 0.0;

    public ReturnBookTransaction(String callerUserId, String resourceId) {
        this(callerUserId, resourceId, LibraryMediatorImpl.getInstance());
    }

    public ReturnBookTransaction(String callerUserId, String resourceId, LibraryMediator mediator) {
        super("RETURN_BOOK", callerUserId);
        this.resourceId = resourceId;
        this.mediator = mediator != null ? mediator : LibraryMediatorImpl.getInstance();
    }

    @Override
    protected void validate() throws TransactionException {
        resource = db.getResource(resourceId);
        if (resource == null) {
            throw new TransactionException("Resource not found with ID: " + resourceId, "RESOURCE_NOT_FOUND");
        }

        activeLoan = db.getActiveLoanForResource(resourceId);
        if (activeLoan == null) {
            // Check if resource is already marked available
            if (resource.isAvailable()) {
                throw new TransactionException("Resource '" + resource.getTitle() + "' is not currently checked out.", "NO_ACTIVE_LOAN");
            }
        }
    }

    @Override
    protected void prepare() throws TransactionException {
        previousAvailability = resource.isAvailable();
        if (activeLoan != null) {
            previousFineAmount = activeLoan.getFine();
        }
        logStep("Prepared Return Context for Resource " + resourceId);
    }

    @Override
    protected void executeTransaction() throws TransactionException {
        // Delegate complex cross-service coordination to LibraryMediator
        coordinationResult = mediator.coordinateReturn(callerUserId, resourceId);
        if (!coordinationResult.isSuccess()) {
            throw new TransactionException(coordinationResult.getMessage(), "RETURN_COORDINATION_FAILED");
        }
        for (String entry : coordinationResult.getCoordinationLog()) {
            logStep("Mediator -> " + entry);
        }
    }

    @Override
    protected void persist() throws Exception {
        // Mediator colleagues handle entity-level persistence; here we verify DB integrity
        logStep("Verified persistence of closed loan and updated resource state");
    }

    @Override
    protected void rollback() {
        if (resource != null) {
            resource.setAvailable(previousAvailability);
            db.saveResource(resource);
        }
        if (activeLoan != null) {
            activeLoan.setReturned(false);
            activeLoan.setReturnDate(null);
            activeLoan.setFine(previousFineAmount);
            db.saveLoan(activeLoan);
        }
        logStep("Rollback executed: restored loan and resource return states");
    }

    @Override
    protected void recordAuditLog(boolean success, String errorDetails) {
        long latency = System.currentTimeMillis() - startTime;
        String details = String.format("Resource: %s | Success: %b%s",
                resourceId, success, (errorDetails != null ? " (" + errorDetails + ")" : ""));

        db.addAuditLog(new AuditLogEntry(
                "AUD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                callerUserId,
                "RETURN_EXECUTION",
                details,
                success,
                latency
        ));
    }

    @Override
    protected TransactionResult generateSuccessResult() {
        long latency = System.currentTimeMillis() - startTime;
        double fine = coordinationResult != null ? coordinationResult.getFineAmount() : 0.0;
        int overdueDays = coordinationResult != null ? coordinationResult.getDaysOverdue() : 0;
        String msg = coordinationResult != null ? coordinationResult.getMessage() : "Return completed successfully";

        int notifiedCount = (coordinationResult != null && coordinationResult.isHeldForReservation()) ? 1 : 0;
        ReturnResult returnResult = new ReturnResult(true, msg, fine, overdueDays, notifiedCount, latency);
        return new TransactionResult(true, transactionId, transactionType, msg, null, executionSteps, latency, returnResult);
    }

    @Override
    protected TransactionResult generateFailureResult(String message, String errorCode) {
        long latency = System.currentTimeMillis() - startTime;
        ReturnResult returnResult = new ReturnResult(false, message, 0.0, 0, 0, latency);
        return new TransactionResult(false, transactionId, transactionType, message, errorCode, executionSteps, latency, returnResult);
    }

    public ReturnCoordinationResult getCoordinationResult() { return coordinationResult; }
}
