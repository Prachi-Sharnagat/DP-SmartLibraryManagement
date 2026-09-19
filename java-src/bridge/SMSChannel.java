package bridge;

import model.User;

public class SMSChannel implements NotificationChannel {
    @Override
    public void sendNotification(User recipient, String title, String message) {
        String phone = recipient.getPhone() != null ? recipient.getPhone() : "unknown-phone";
        System.out.printf("  📱  [SMS -> %s] ALERT: %s - %s%n", phone, title, message);
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }
}
