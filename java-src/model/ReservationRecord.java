package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReservationRecord {
    private final String id;
    private final String resourceId;
    private final String resourceTitle;
    private final String userId;
    private final String userName;
    private final String requestedDate;
    private String status; // PENDING, NOTIFIED, COMPLETED, CANCELLED

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReservationRecord(String id, String resourceId, String resourceTitle, String userId, String userName) {
        this.id = id;
        this.resourceId = resourceId;
        this.resourceTitle = resourceTitle;
        this.userId = userId;
        this.userName = userName;
        this.requestedDate = LocalDateTime.now().format(FORMATTER);
        this.status = "PENDING";
    }

    public String getId() { return id; }
    public String getResourceId() { return resourceId; }
    public String getResourceTitle() { return resourceTitle; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getRequestedDate() { return requestedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String toJson() {
        return String.format(
                "{\"id\":\"%s\",\"resourceId\":\"%s\",\"resourceTitle\":\"%s\",\"userId\":\"%s\",\"userName\":\"%s\",\"requestedDate\":\"%s\",\"status\":\"%s\"}",
                id, resourceId, resourceTitle != null ? resourceTitle.replace("\"", "\\\"") : "",
                userId, userName != null ? userName.replace("\"", "\\\"") : "",
                requestedDate, status
        );
    }
}
