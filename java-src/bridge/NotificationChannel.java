package bridge;

/**
 * Implementor interface in the Bridge Pattern.
 */
public interface NotificationChannel {
    void sendNotification(String recipient, String title, String message);
    String getChannelName();
}
