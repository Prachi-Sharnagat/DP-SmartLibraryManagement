package chain;

import factory.LibraryResource;
import model.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates whether resource has an active reservation queue held by another member.
 */
public class ReservationHandler extends ValidationHandler {
    private static final Map<String, String> activeReservations = new ConcurrentHashMap<>();

    public static void setReservation(String resourceId, String reservedByUserId) {
        activeReservations.put(resourceId, reservedByUserId);
    }

    public static void removeReservation(String resourceId) {
        activeReservations.remove(resourceId);
    }

    public static String getReservedUser(String resourceId) {
        return activeReservations.get(resourceId);
    }

    @Override
    public ValidationResult validate(User user, LibraryResource resource) {
        String reservedUser = activeReservations.get(resource.getId());
        if (reservedUser != null && !reservedUser.equalsIgnoreCase(user.getUserId())) {
            return ValidationResult.failure("ReservationHandler",
                    "Resource '" + resource.getTitle() + "' is currently reserved for member ID: " + reservedUser + ". You cannot borrow it until the reservation expires or is claimed.");
        }

        return checkNext(user, resource);
    }
}
