package abstractfactory;

/**
 * Product A in the Abstract Factory Pattern: Loan & Circulation Policy.
 * Defines circulation constraints, borrowing durations, and renewal rules for a media family.
 */
public interface LoanPolicy {
    String getPolicyName();
    int getBorrowDurationDays();
    double getDailyFineRate();
    boolean isRenewable();
    String getDeliveryMode();
    String getPolicySummary();
}
