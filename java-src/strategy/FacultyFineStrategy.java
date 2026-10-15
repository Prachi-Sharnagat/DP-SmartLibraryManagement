package strategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Faculty Fine Strategy designed for academic research personnel.
 * Research privilege rate of $0.10 / day, extended 7-day grace period, capped at $10.00.
 */
public class FacultyFineStrategy implements FineCalculationStrategy {
    private static final String STRATEGY_NAME = "Faculty Academic Privilege Policy";
    private static final double DAILY_RATE = 0.10;
    private static final int GRACE_PERIOD_DAYS = 7;
    private static final double MAX_FINE_LIMIT = 10.00;

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
