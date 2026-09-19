package bridge;

import model.User;

/**
 * Implementor interface in the Bridge Pattern.
 */
public interface NotificationChannel {
    void sendNotification(User recipient, String title, String message);
    String getChannelName();
}
