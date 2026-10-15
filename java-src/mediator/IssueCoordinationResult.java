package mediator;

import model.Loan;

import java.util.Collections;
import java.util.List;

/**
 * Result data transfer object produced by LibraryMediator issue coordination.
 */
public class IssueCoordinationResult {
    private final boolean success;
    private final String message;
    private final Loan createdLoan;
    private final List<String> coordinationLog;
    private final long latencyMs;

    public IssueCoordinationResult(boolean success, String message, Loan createdLoan,
                                   List<String> coordinationLog, long latencyMs) {
        this.success = success;
        this.message = message;
        this.createdLoan = createdLoan;
        this.coordinationLog = coordinationLog != null ? coordinationLog : Collections.emptyList();
        this.latencyMs = latencyMs;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Loan getCreatedLoan() { return createdLoan; }
    public List<String> getCoordinationLog() { return coordinationLog; }
    public long getLatencyMs() { return latencyMs; }
}
