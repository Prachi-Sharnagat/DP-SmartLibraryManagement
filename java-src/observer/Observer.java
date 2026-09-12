package observer;

/**
 * Observer interface in the Observer Pattern.
 */
public interface Observer {
    String getObserverId();
    String getObserverName();
    void update(String resourceId, String resourceTitle, String eventType, String details);
}
