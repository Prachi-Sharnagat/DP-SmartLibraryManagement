package bridge;

import model.User;

public class InAppChannel implements NotificationChannel {
    @Override
    public void sendNotification(User recipient, String title, String message) {
        System.out.printf("  🔔  [IN-APP PUSH -> %s] [%s] %s%n", recipient.getUserId(), title, message);
    }

    @Override
    public String getChannelName() {
        return "IN_APP";
    }
}
