package observer;

import bridge.Notification;
import model.User;

import java.util.Objects;

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
            String message = String.format("Resource '%s' has a new update: %s. (%s)", resourceTitle, eventType, details);
            notificationBridge.send(user, message);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(getObserverId(), member.getObserverId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObserverId());
    }
}
