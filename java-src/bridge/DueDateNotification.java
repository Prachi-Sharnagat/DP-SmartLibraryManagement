package bridge;

import model.User;

/**
 * Refined Abstraction in the Bridge Pattern.
 */
public class DueDateNotification extends Notification {

    public DueDateNotification(NotificationChannel channel) {
        super(channel);
    }

    @Override
    public void send(User recipient, String message) {
        String title = "Smart Library Reminder: Upcoming Due Date";
        channel.sendNotification(recipient, title, message);
    }
}
