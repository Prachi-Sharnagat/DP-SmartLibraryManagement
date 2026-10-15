package template;

/**
 * Checked exception for library transaction pipeline failures.
 */
public class TransactionException extends Exception {
    private final String errorCode;

    public TransactionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : "TRANSACTION_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
