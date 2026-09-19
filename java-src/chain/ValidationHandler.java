package chain;

import factory.LibraryResource;
import model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Base Handler in the Chain of Responsibility Pattern.
 */
public abstract class ValidationHandler {
    protected ValidationHandler nextHandler;

    public ValidationHandler setNext(ValidationHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }

    public abstract ValidationResult validate(User user, LibraryResource resource);

    protected ValidationResult checkNext(User user, LibraryResource resource) {
        if (nextHandler != null) {
            return nextHandler.validate(user, resource);
        }
        return ValidationResult.success("All library validation checks passed successfully.");
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final String failedHandlerName;
        private final List<String> executionTrace; // Phase 20: Keep track of execution for UI

        private ValidationResult(boolean valid, String message, String failedHandlerName) {
            this.valid = valid;
            this.message = message;
            this.failedHandlerName = failedHandlerName;
            this.executionTrace = new ArrayList<>();
        }

        public static ValidationResult success(String message) {
            return new ValidationResult(true, message, null);
        }

        public static ValidationResult failure(String handlerName, String reason) {
            return new ValidationResult(false, reason, handlerName);
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getFailedHandlerName() { return failedHandlerName; }
        public List<String> getExecutionTrace() { return executionTrace; }

        public void addTrace(String handlerName, boolean passed) {
            executionTrace.add(handlerName + (passed ? " ✓ PASS" : " ✗ FAIL"));
        }

        @Override
        public String toString() {
            return valid ? "[VALIDATION PASSED] " + message : "[VALIDATION FAILED at " + failedHandlerName + "] " + message;
        }
    }
}
