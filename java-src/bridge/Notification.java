package bridge;

/**
 * Abstraction in the Bridge Pattern.
 * Decouples the notification content/logic from the delivery mechanism channel.
 */
public abstract class Notification {
    protected NotificationChannel channel;

    public Notification(NotificationChannel channel) {
        this.channel = channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public abstract void send(String recipient, String message);
}
