package abstractfactory;

/**
 * Concrete Product A1: Physical Print Media Circulation Policy.
 */
public class PhysicalLoanPolicy implements LoanPolicy {
    private final int borrowDays = 14;
    private final double dailyFineRate = 1.50;
    private final boolean renewable = true;

    @Override
    public String getPolicyName() {
        return "Standard Physical Print Loan Policy";
    }

    @Override
    public int getBorrowDurationDays() {
        return borrowDays;
    }

    @Override
    public double getDailyFineRate() {
        return dailyFineRate;
    }

    @Override
    public boolean isRenewable() {
        return renewable;
    }

    @Override
    public String getDeliveryMode() {
        return "PHYSICAL_CIRCULATION_DESK";
    }

    @Override
    public String getPolicySummary() {
        return "14-day checkout with physical desk return; 1 renewal permitted; $1.50/day late fee.";
    }
}
