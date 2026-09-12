package chain;

import factory.LibraryResource;
import model.User;

/**
 * Validates that user membership is active.
 */
public class MembershipHandler extends ValidationHandler {

    @Override
    public ValidationResult validate(User user, LibraryResource resource) {
        if (user == null) {
            return ValidationResult.failure("MembershipHandler", "User account not found.");
        }

        if (!user.isMembershipActive()) {
            return ValidationResult.failure("MembershipHandler",
                    "User membership for '" + user.getName() + "' (ID: " + user.getUserId() + ") is inactive or expired. Please renew membership.");
        }

        return checkNext(user, resource);
    }
}
