package strategy;

import java.time.LocalDate;

/**
 * Strategy Design Pattern interface for dynamic fine calculation and overdue policies.
 */
public interface FineCalculationStrategy {
    String getStrategyName();
    double getDailyRate();
    int getGracePeriodDays();
    double getMaxFineLimit();
    FineCalculationResult calculateFine(LocalDate dueDate, LocalDate returnDate);
}
