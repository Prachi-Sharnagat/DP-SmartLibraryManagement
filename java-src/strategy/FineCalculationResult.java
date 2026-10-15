package strategy;

import java.util.Locale;

/**
 * Result data carrier encapsulating the breakdown of a strategy-based fine calculation.
 */
public class FineCalculationResult {
    private final String strategyName;
    private final int daysOverdue;
    private final int graceDaysApplied;
    private final int billableDays;
    private final double dailyRate;
    private final double calculatedFine;
    private final boolean isCappedAtMax;
    private final double maxFineLimit;

    public FineCalculationResult(String strategyName, int daysOverdue, int graceDaysApplied,
                                 int billableDays, double dailyRate, double calculatedFine,
                                 boolean isCappedAtMax, double maxFineLimit) {
        this.strategyName = strategyName;
        this.daysOverdue = Math.max(0, daysOverdue);
        this.graceDaysApplied = Math.max(0, graceDaysApplied);
        this.billableDays = Math.max(0, billableDays);
        this.dailyRate = dailyRate;
        this.calculatedFine = Math.max(0.0, calculatedFine);
        this.isCappedAtMax = isCappedAtMax;
        this.maxFineLimit = maxFineLimit;
    }

    public String getStrategyName() { return strategyName; }
    public int getDaysOverdue() { return daysOverdue; }
    public int getGraceDaysApplied() { return graceDaysApplied; }
    public int getBillableDays() { return billableDays; }
    public double getDailyRate() { return dailyRate; }
    public double getCalculatedFine() { return calculatedFine; }
    public boolean isCappedAtMax() { return isCappedAtMax; }
    public double getMaxFineLimit() { return maxFineLimit; }

    public String getSummary() {
        return String.format(Locale.US,
                "Policy: %s | Overdue: %d day(s) | Grace: %d day(s) | Billable: %d day(s) @ $%.2f/day | Fine: $%.2f%s",
                strategyName, daysOverdue, graceDaysApplied, billableDays, dailyRate, calculatedFine,
                (isCappedAtMax ? " (Capped at maximum limit of $" + String.format(Locale.US, "%.2f", maxFineLimit) + ")" : ""));
    }

    public String toJson() {
        return String.format(Locale.US,
                "{\"strategyName\":\"%s\",\"daysOverdue\":%d,\"graceDaysApplied\":%d,\"billableDays\":%d,\"dailyRate\":%.2f,\"calculatedFine\":%.2f,\"isCappedAtMax\":%b,\"maxFineLimit\":%.2f}",
                strategyName, daysOverdue, graceDaysApplied, billableDays, dailyRate, calculatedFine, isCappedAtMax, maxFineLimit);
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
