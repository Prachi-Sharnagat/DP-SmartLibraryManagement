package observer;

import bridge.Notification;
import model.User;

/**
 * Concrete Observer representing a library Member subscribed to resource notifications.
 * Uses the Bridge Pattern (Notification) to deliver alerts via configured channels.
 */
public class Member implements Observer {
    private final User user;
    private Notification notificationBridge;

    public Member(User user, Notification notificationBridge) {
        this.user = user;
        this.notificationBridge = notificationBridge;
    }

    public User getUser() {
        return user;
    }

    public void setNotificationBridge(Notification notificationBridge) {
        this.notificationBridge = notificationBridge;
    }

    @Override
    public String getObserverId() {
        return user.getUserId();
    }

    @Override
    public String getObserverName() {
        return user.getName();
    }

    @Override
    public void update(String resourceId, String resourceTitle, String eventType, String details) {
        System.out.printf("  [Observer: Member '%s' (ID: %s)] Received Event [%s] for Resource: '%s' (ID: %s)%n",
                user.getName(), user.getUserId(), eventType, resourceTitle, resourceId);

        if (notificationBridge != null) {
            String recipient;
            String chName = notificationBridge.getChannel() != null ? notificationBridge.getChannel().getChannelName() : "EMAIL";
            if ("SMS".equalsIgnoreCase(chName)) {
                recipient = user.getPhone() != null ? user.getPhone() : user.getEmail();
            } else if ("IN_APP".equalsIgnoreCase(chName)) {
                recipient = user.getUserId();
            } else {
                recipient = user.getEmail() != null ? user.getEmail() : user.getPhone();
            }
            String message = String.format("Resource '%s' has a new update: %s. (%s)", resourceTitle, eventType, details);
            notificationBridge.send(recipient, message);
        }
    }
}
