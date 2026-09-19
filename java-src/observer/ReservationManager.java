package observer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concrete Subject managing resource reservations and waitlists using Observer Pattern.
 */
public class ReservationManager implements Subject {
    private static ReservationManager instance;
    private final Map<String, Set<Observer>> resourceWaitlists = new ConcurrentHashMap<>();

    public static synchronized ReservationManager getInstance() {
        if (instance == null) {
            instance = new ReservationManager();
        }
        return instance;
    }

    @Override
    public void registerObserver(String resourceId, Observer observer) {
        resourceWaitlists.computeIfAbsent(resourceId, k -> new LinkedHashSet<>()).add(observer);
        System.out.printf("  [ReservationManager] Observer '%s' registered for Resource ID: %s%n",
                observer.getObserverName(), resourceId);
    }

    @Override
    public void removeObserver(String resourceId, Observer observer) {
        Set<Observer> observers = resourceWaitlists.get(resourceId);
        if (observers != null) {
            observers.remove(observer);
            System.out.printf("  [ReservationManager] Observer '%s' removed from waitlist for Resource ID: %s%n",
                    observer.getObserverName(), resourceId);
        }
    }

    @Override
    public void notifyObservers(String resourceId, String resourceTitle, String eventType, String details) {
        Set<Observer> observers = resourceWaitlists.get(resourceId);
        if (observers != null && !observers.isEmpty()) {
            System.out.printf("  [ReservationManager] Broadcasting '%s' event for '%s' to %d observer(s)...%n",
                    eventType, resourceTitle, observers.size());
            // Make a copy to avoid concurrent modification if observers unregister during callback
            List<Observer> copy = new ArrayList<>(observers);
            for (Observer obs : copy) {
                try {
                    obs.update(resourceId, resourceTitle, eventType, details);
                } catch (Exception e) {
                    System.err.printf("  [ReservationManager] Error notifying observer '%s': %s%n",
                            obs.getObserverName(), e.getMessage());
                }
            }
        } else {
            System.out.printf("  [ReservationManager] No waitlist observers registered for Resource ID: %s%n", resourceId);
        }
    }

    public List<Observer> getWaitlist(String resourceId) {
        return new ArrayList<>(resourceWaitlists.getOrDefault(resourceId, new LinkedHashSet<>()));
    }

    public void clearWaitlist(String resourceId) {
        resourceWaitlists.remove(resourceId);
    }
}
