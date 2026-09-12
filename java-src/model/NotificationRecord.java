package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationRecord {
    private final String id;
    private final String timestamp;
    private final String recipientUserId;
    private final String recipientName;
    private final String channel;
    private final String title;
    private final String message;
    private final String status;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotificationRecord(String id, String recipientUserId, String recipientName, String channel, String title, String message, String status) {
        this.id = id;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
        this.recipientUserId = recipientUserId;
        this.recipientName = recipientName;
        this.channel = channel;
        this.title = title;
        this.message = message;
        this.status = status;
    }

    public String getId() { return id; }
    public String getTimestamp() { return timestamp; }
    public String getRecipientUserId() { return recipientUserId; }
    public String getRecipientName() { return recipientName; }
    public String getChannel() { return channel; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }

    public String toJson() {
        return String.format(
                "{\"id\":\"%s\",\"timestamp\":\"%s\",\"recipientUserId\":\"%s\",\"recipientName\":\"%s\",\"channel\":\"%s\",\"title\":\"%s\",\"message\":\"%s\",\"status\":\"%s\"}",
                id, timestamp, recipientUserId, recipientName != null ? recipientName.replace("\"", "\\\"") : "",
                channel, title != null ? title.replace("\"", "\\\"") : "",
                message != null ? message.replace("\"", "\\\"") : "",
                status
        );
    }
}
