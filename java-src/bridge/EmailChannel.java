package bridge;

import model.User;

public class EmailChannel implements NotificationChannel {
    @Override
    public void sendNotification(User recipient, String title, String message) {
        String email = recipient.getEmail() != null ? recipient.getEmail() : "unknown@lib.org";
        System.out.printf("  ✉️  [EMAIL -> %s] Subject: %s | Body: %s%n", email, title, message);
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }
}
