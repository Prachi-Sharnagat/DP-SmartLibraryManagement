package bridge;

public class InAppChannel implements NotificationChannel {
    @Override
    public void sendNotification(String recipient, String title, String message) {
        System.out.printf("  🔔  [IN-APP PUSH -> %s] [%s] %s%n", recipient, title, message);
    }

    @Override
    public String getChannelName() {
        return "IN_APP";
    }
}
