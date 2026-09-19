package bridge;

import model.User;

/**
 * Refined Abstraction in the Bridge Pattern.
 */
public class BookAvailableNotification extends Notification {

    public BookAvailableNotification(NotificationChannel channel) {
        super(channel);
    }

    @Override
    public void send(User recipient, String message) {
        String title = "Smart Library Alert: Book Available for Pickup";
        channel.sendNotification(recipient, title, message);
    }

    public void sendWithDetails(User recipient, String bookTitle, String rackLocation) {
        String title = "Smart Library Alert: Reserved Book Available";
        String body = String.format("Good news! '%s' has been returned and is waiting for you at %s. Please collect it within 48 hours.",
                bookTitle, rackLocation);
        channel.sendNotification(recipient, title, body);
    }
}
