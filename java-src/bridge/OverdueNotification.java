package bridge;

import model.User;

/**
 * Refined Abstraction in the Bridge Pattern.
 */
public class OverdueNotification extends Notification {

    public OverdueNotification(NotificationChannel channel) {
        super(channel);
    }

    @Override
    public void send(User recipient, String message) {
        String title = "Smart Library Alert: Item Overdue";
        channel.sendNotification(recipient, title, message);
    }
}
