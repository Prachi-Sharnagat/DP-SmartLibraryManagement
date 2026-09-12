package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogEntry {
    private final String id;
    private final String timestamp;
    private final String callerUserId;
    private final String action;
    private final String details;
    private final boolean success;
    private final long latencyMs;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLogEntry(String id, String callerUserId, String action, String details, boolean success, long latencyMs) {
        this.id = id;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
        this.callerUserId = callerUserId != null ? callerUserId : "ANONYMOUS";
        this.action = action;
        this.details = details;
        this.success = success;
        this.latencyMs = latencyMs;
    }

    public String getId() { return id; }
    public String getTimestamp() { return timestamp; }
    public String getCallerUserId() { return callerUserId; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public boolean isSuccess() { return success; }
    public long getLatencyMs() { return latencyMs; }

    public String toJson() {
        return String.format(
                "{\"id\":\"%s\",\"timestamp\":\"%s\",\"callerUserId\":\"%s\",\"action\":\"%s\",\"details\":\"%s\",\"success\":%b,\"latencyMs\":%d}",
                id, timestamp, callerUserId, action, details != null ? details.replace("\"", "\\\"") : "", success, latencyMs
        );
    }
}
