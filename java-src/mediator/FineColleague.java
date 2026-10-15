package mediator;

import database.Database;
import model.User;
import strategy.FineCalculationResult;
import strategy.FineCalculationStrategy;
import strategy.FineStrategyFactory;

import java.time.LocalDate;

/**
 * Colleague handling fine calculation and member account balance updates.
 * Leverages Strategy Pattern to dynamically resolve fine policies per member category.
 */
public class FineColleague extends Colleague {
    private final Database db;

    public FineColleague(Database db) {
        this.db = db;
    }

    public FineCalculationResult assessReturnFine(String borrowerId, LocalDate dueDate, LocalDate returnDate) {
        User user = db.getUser(borrowerId);
        FineCalculationStrategy strategy = FineStrategyFactory.getStrategy(user);

        FineCalculationResult result = strategy.calculateFine(dueDate, returnDate);

        if (result.getCalculatedFine() > 0.0 && user != null) {
            user.setOutstandingFine(user.getOutstandingFine() + result.getCalculatedFine());
            db.saveUser(user);
        }

        return result;
    }
}
