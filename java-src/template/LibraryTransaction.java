package template;

import database.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstract Class defining the Template Method for library transactions (e.g. Issue, Return).
 * Standardizes the 6-step transaction lifecycle:
 * 1. validate()
 * 2. prepare()
 * 3. executeTransaction()
 * 4. persist()
 * 5. postTransaction()
 * 6. recordAuditLog()
 *
 * Guarantees atomic rollback upon database or execution failure, preventing partial transaction corruption.
 */
public abstract class LibraryTransaction {
    protected final Database db = Database.getInstance();
    protected final String transactionId;
    protected final String transactionType;
    protected final String callerUserId;
    protected final long startTime;
    protected final List<String> executionSteps = new ArrayList<>();

    public LibraryTransaction(String transactionType, String callerUserId) {
        this.transactionType = transactionType;
        this.callerUserId = callerUserId != null ? callerUserId : "SYSTEM";
        this.transactionId = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.startTime = System.currentTimeMillis();
    }

    /**
     * The Template Method. Marked final so subclasses cannot violate the transactional sequence.
     */
    public final TransactionResult execute() {
        try {
            logStep("1. Validating Transaction Preconditions");
            validate();

            logStep("2. Preparing Context & Calculating Transaction Details");
            prepare();

            logStep("3. Executing Core Business Mutation");
            executeTransaction();

            logStep("4. Persisting Changes to Database Store");
            persist();

            logStep("5. Executing Post-Transaction Hooks & Mediator Coordination");
            postTransaction();

            logStep("6. Recording Audit Log & Latency Telemetry");
            recordAuditLog(true, null);

            return generateSuccessResult();

        } catch (TransactionException te) {
            logStep("Transaction Aborted: " + te.getMessage());
            rollback();
            recordAuditLog(false, te.getMessage());
            return generateFailureResult(te.getMessage(), te.getErrorCode());

        } catch (Exception e) {
            logStep("Database / Persistence Failure: " + e.getMessage());
            rollback();
            recordAuditLog(false, e.getMessage());
            return generateFailureResult("Transaction failed during execution: " + e.getMessage(), "PERSISTENCE_FAILURE");
        }
    }

    protected void logStep(String step) {
        executionSteps.add(String.format("[%s @ +%dms] %s", transactionType, (System.currentTimeMillis() - startTime), step));
    }

    // Abstract steps customized by subclasses
    protected abstract void validate() throws TransactionException;
    protected abstract void prepare() throws TransactionException;
    protected abstract void executeTransaction() throws TransactionException;
    protected abstract void persist() throws Exception;
    protected abstract void rollback();
    protected abstract void recordAuditLog(boolean success, String errorDetails);
    protected abstract TransactionResult generateSuccessResult();
    protected abstract TransactionResult generateFailureResult(String message, String errorCode);

    // Optional hook method for subclasses
    protected void postTransaction() {
        // default no-op hook
    }

    public String getTransactionId() { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public List<String> getExecutionSteps() { return new ArrayList<>(executionSteps); }
}
