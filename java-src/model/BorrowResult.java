package model;

public class BorrowResult {
    private final boolean success;
    private final String message;
    private final String errorCode;
    private final String failedRule;
    private final Loan loan;
    private final long latencyMs;

    public BorrowResult(boolean success, String message, String errorCode, String failedRule, Loan loan, long latencyMs) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.failedRule = failedRule;
        this.loan = loan;
        this.latencyMs = latencyMs;
    }

    public static BorrowResult ok(String message, Loan loan, long latencyMs) {
        return new BorrowResult(true, message, null, null, loan, latencyMs);
    }

    public static BorrowResult fail(String message, String errorCode, String failedRule, long latencyMs) {
        return new BorrowResult(false, message, errorCode, failedRule, null, latencyMs);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
    public String getFailedRule() { return failedRule; }
    public Loan getLoan() { return loan; }
    public long getLatencyMs() { return latencyMs; }

    public String toJson() {
        return String.format(
                "{\"success\":%b,\"message\":\"%s\",\"errorCode\":%s,\"failedRule\":%s,\"loan\":%s,\"latencyMs\":%d}",
                success,
                message != null ? message.replace("\"", "\\\"") : "",
                errorCode != null ? "\"" + errorCode + "\"" : "null",
                failedRule != null ? "\"" + failedRule + "\"" : "null",
                loan != null ? String.format("{\"loanId\":\"%s\",\"userId\":\"%s\",\"resourceId\":\"%s\",\"dueDate\":\"%s\"}", loan.getLoanId(), loan.getUserId(), loan.getResourceId(), loan.getDueDate()) : "null",
                latencyMs
        );
    }
}
