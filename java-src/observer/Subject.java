package observer;

/**
 * Subject interface in the Observer Pattern.
 */
public interface Subject {
    void registerObserver(String resourceId, Observer observer);
    void removeObserver(String resourceId, Observer observer);
    void notifyObservers(String resourceId, String resourceTitle, String eventType, String details);
}
