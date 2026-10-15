package strategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Student Fine Strategy designed for academic student welfare.
 * Subsidized rate of $0.50 / day, extended 3-day grace period, capped at $20.00.
 */
public class StudentFineStrategy implements FineCalculationStrategy {
    private static final String STRATEGY_NAME = "Student Subsidized Fine Policy";
    private static final double DAILY_RATE = 0.50;
    private static final int GRACE_PERIOD_DAYS = 3;
    private static final double MAX_FINE_LIMIT = 20.00;

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public double getDailyRate() {
        return DAILY_RATE;
    }

    @Override
    public int getGracePeriodDays() {
        return GRACE_PERIOD_DAYS;
    }

    @Override
    public double getMaxFineLimit() {
        return MAX_FINE_LIMIT;
    }

    @Override
    public FineCalculationResult calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate == null || returnDate == null || !returnDate.isAfter(dueDate)) {
            return new FineCalculationResult(STRATEGY_NAME, 0, 0, 0, DAILY_RATE, 0.0, false, MAX_FINE_LIMIT);
        }

        int daysOverdue = (int) ChronoUnit.DAYS.between(dueDate, returnDate);
        if (daysOverdue <= 0) {
            return new FineCalculationResult(STRATEGY_NAME, 0, 0, 0, DAILY_RATE, 0.0, false, MAX_FINE_LIMIT);
        }

        int graceDays = Math.min(daysOverdue, GRACE_PERIOD_DAYS);
        int billableDays = Math.max(0, daysOverdue - GRACE_PERIOD_DAYS);
        double rawFine = billableDays * DAILY_RATE;
        boolean isCapped = rawFine > MAX_FINE_LIMIT;
        double finalFine = isCapped ? MAX_FINE_LIMIT : rawFine;

        return new FineCalculationResult(STRATEGY_NAME, daysOverdue, graceDays, billableDays, DAILY_RATE, finalFine, isCapped, MAX_FINE_LIMIT);
    }
}
