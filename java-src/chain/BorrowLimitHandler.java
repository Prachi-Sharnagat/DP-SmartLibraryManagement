package chain;

import factory.LibraryResource;
import model.User;

/**
 * Validates that user hasn't exceeded the maximum active borrow count.
 */
public class BorrowLimitHandler extends ValidationHandler {
    private static final int MAX_BORROW_LIMIT = 5;

    @Override
    public ValidationResult validate(User user, LibraryResource resource) {
        if (user.getCurrentBorrowedCount() >= MAX_BORROW_LIMIT) {
            return ValidationResult.failure("BorrowLimitHandler",
                    String.format("User has already reached maximum borrowing limit of %d items (Currently borrowed: %d). Return items first.",
                            MAX_BORROW_LIMIT, user.getCurrentBorrowedCount()));
        }

        return checkNext(user, resource);
    }
}
