package mediator;

import model.Loan;
import strategy.FineCalculationResult;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Result data transfer object produced by LibraryMediator return coordination.
 */
public class ReturnCoordinationResult {
    private final boolean success;
    private final String message;
    private final Loan closedLoan;
    private final FineCalculationResult fineResult;
    private final boolean heldForReservation;
    private final String nextReservedUserId;
    private final List<String> coordinationLog;
    private final long latencyMs;

    public ReturnCoordinationResult(boolean success, String message, Loan closedLoan,
                                    FineCalculationResult fineResult, boolean heldForReservation,
                                    String nextReservedUserId, List<String> coordinationLog, long latencyMs) {
        this.success = success;
        this.message = message;
        this.closedLoan = closedLoan;
        this.fineResult = fineResult;
        this.heldForReservation = heldForReservation;
        this.nextReservedUserId = nextReservedUserId;
        this.coordinationLog = coordinationLog != null ? coordinationLog : Collections.emptyList();
        this.latencyMs = latencyMs;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Loan getClosedLoan() { return closedLoan; }
    public FineCalculationResult getFineResult() { return fineResult; }
    public boolean isHeldForReservation() { return heldForReservation; }
    public String getNextReservedUserId() { return nextReservedUserId; }
    public List<String> getCoordinationLog() { return coordinationLog; }
    public long getLatencyMs() { return latencyMs; }

    public double getFineAmount() {
        return fineResult != null ? fineResult.getCalculatedFine() : 0.0;
    }

    public int getDaysOverdue() {
        return fineResult != null ? fineResult.getDaysOverdue() : 0;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"success\":").append(success).append(",");
        sb.append("\"message\":\"").append(server.JsonUtils.escape(message)).append("\",");
        sb.append("\"heldForReservation\":").append(heldForReservation).append(",");
        sb.append("\"nextReservedUserId\":").append(nextReservedUserId != null ? "\"" + nextReservedUserId + "\"" : "null").append(",");
        sb.append("\"daysOverdue\":").append(getDaysOverdue()).append(",");
        sb.append(String.format(Locale.US, "\"fine\":%.2f,", getFineAmount()));
        sb.append("\"fineDetails\":").append(fineResult != null ? fineResult.toJson() : "null").append(",");
        sb.append("\"latencyMs\":").append(latencyMs).append(",");
        sb.append("\"coordinationLog\":[");
        for (int i = 0; i < coordinationLog.size(); i++) {
            sb.append("\"").append(server.JsonUtils.escape(coordinationLog.get(i))).append("\"");
            if (i < coordinationLog.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }
}
