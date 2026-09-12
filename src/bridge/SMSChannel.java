package bridge;

public class SMSChannel implements NotificationChannel {
    @Override
    public void sendNotification(String recipient, String title, String message) {
        System.out.printf("  📱  [SMS -> %s] ALERT: %s - %s%n", recipient, title, message);
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }
}
