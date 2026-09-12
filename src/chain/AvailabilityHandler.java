package chain;

import factory.LibraryResource;
import model.User;

/**
 * Validates that resource is borrowable and physically/digitally available.
 */
public class AvailabilityHandler extends ValidationHandler {

    @Override
    public ValidationResult validate(User user, LibraryResource resource) {
        if (resource == null) {
            return ValidationResult.failure("AvailabilityHandler", "Resource does not exist in library records.");
        }

        if (!resource.isBorrowable()) {
            return ValidationResult.failure("AvailabilityHandler",
                    "Resource '" + resource.getTitle() + "' (ID: " + resource.getId() + ") is marked as REFERENCE ONLY and cannot be checked out.");
        }

        if (!resource.isAvailable()) {
            return ValidationResult.failure("AvailabilityHandler",
                    "Resource '" + resource.getTitle() + "' (ID: " + resource.getId() + ") is currently checked out by another borrower.");
        }

        return checkNext(user, resource);
    }
}
