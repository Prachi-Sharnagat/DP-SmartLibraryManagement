package observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concrete Subject managing resource reservations and waitlists using Observer Pattern.
 */
public class ReservationManager implements Subject {
    private static ReservationManager instance;
    private final Map<String, List<Observer>> resourceWaitlists = new ConcurrentHashMap<>();

    public static synchronized ReservationManager getInstance() {
        if (instance == null) {
            instance = new ReservationManager();
        }
        return instance;
    }

    @Override
    public void registerObserver(String resourceId, Observer observer) {
        resourceWaitlists.computeIfAbsent(resourceId, k -> new ArrayList<>()).add(observer);
        System.out.printf("  [ReservationManager] Observer '%s' registered for Resource ID: %s%n",
                observer.getObserverName(), resourceId);
    }

    @Override
    public void removeObserver(String resourceId, Observer observer) {
        List<Observer> observers = resourceWaitlists.get(resourceId);
        if (observers != null) {
            observers.remove(observer);
            System.out.printf("  [ReservationManager] Observer '%s' removed from waitlist for Resource ID: %s%n",
                    observer.getObserverName(), resourceId);
        }
    }

    @Override
    public void notifyObservers(String resourceId, String resourceTitle, String eventType, String details) {
        List<Observer> observers = resourceWaitlists.get(resourceId);
        if (observers != null && !observers.isEmpty()) {
            System.out.printf("  [ReservationManager] Broadcasting '%s' event for '%s' to %d observer(s)...%n",
                    eventType, resourceTitle, observers.size());
            // Make a copy to avoid concurrent modification if observers unregister during callback
            List<Observer> copy = new ArrayList<>(observers);
            for (Observer obs : copy) {
                obs.update(resourceId, resourceTitle, eventType, details);
            }
        } else {
            System.out.printf("  [ReservationManager] No waitlist observers registered for Resource ID: %s%n", resourceId);
        }
    }

    public List<Observer> getWaitlist(String resourceId) {
        return resourceWaitlists.getOrDefault(resourceId, new ArrayList<>());
    }

    public void clearWaitlist(String resourceId) {
        resourceWaitlists.remove(resourceId);
    }
}
