package model;

public class ReturnResult {
    private final boolean success;
    private final String message;
    private final double fine;
    private final int daysOverdue;
    private final int observersNotified;
    private final long latencyMs;

    public ReturnResult(boolean success, String message, double fine, int daysOverdue, int observersNotified, long latencyMs) {
        this.success = success;
        this.message = message;
        this.fine = fine;
        this.daysOverdue = daysOverdue;
        this.observersNotified = observersNotified;
        this.latencyMs = latencyMs;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public double getFine() { return fine; }
    public int getDaysOverdue() { return daysOverdue; }
    public int getObserversNotified() { return observersNotified; }
    public long getLatencyMs() { return latencyMs; }

    public String toJson() {
        return String.format(
                "{\"success\":%b,\"message\":\"%s\",\"fine\":%.2f,\"daysOverdue\":%d,\"observersNotified\":%d,\"latencyMs\":%d}",
                success,
                message != null ? message.replace("\"", "\\\"") : "",
                fine, daysOverdue, observersNotified, latencyMs
        );
    }
}
