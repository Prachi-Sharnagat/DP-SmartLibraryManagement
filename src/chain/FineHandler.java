package chain;

import factory.LibraryResource;
import model.User;

/**
 * Validates that user does not have unpaid fines exceeding the allowed threshold.
 */
public class FineHandler extends ValidationHandler {
    private static final double MAX_ALLOWED_FINE = 10.00;

    @Override
    public ValidationResult validate(User user, LibraryResource resource) {
        if (user.getOutstandingFine() > MAX_ALLOWED_FINE) {
            return ValidationResult.failure("FineHandler",
                    String.format("User has unpaid fines of $%.2f which exceeds maximum allowed threshold ($%.2f). Fines must be settled before borrowing.",
                            user.getOutstandingFine(), MAX_ALLOWED_FINE));
        }

        return checkNext(user, resource);
    }
}
