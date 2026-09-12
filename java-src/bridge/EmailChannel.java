package bridge;

public class EmailChannel implements NotificationChannel {
    @Override
    public void sendNotification(String recipient, String title, String message) {
        System.out.printf("  ✉️  [EMAIL -> %s] Subject: %s | Body: %s%n", recipient, title, message);
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }
}
