package strategy;

import model.User;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Strategy Selector and Policy Registry for Fine Calculation.
 * Selects the appropriate fine calculation strategy dynamically based on member category or configuration.
 */
public class FineStrategyFactory {
    private static final Map<String, FineCalculationStrategy> policyRegistry = new ConcurrentHashMap<>();

    static {
        policyRegistry.put("STUDENT", new StudentFineStrategy());
        policyRegistry.put("FACULTY", new FacultyFineStrategy());
        policyRegistry.put("MEMBER", new StandardFineStrategy());
        policyRegistry.put("ADMIN", new StandardFineStrategy());
        policyRegistry.put("STANDARD", new StandardFineStrategy());
    }

    public static FineCalculationStrategy getStrategy(User user) {
        if (user == null || user.getRole() == null) {
            return policyRegistry.get("STANDARD");
        }
        return getStrategyForRole(user.getRole());
    }

    public static FineCalculationStrategy getStrategyForRole(String role) {
        if (role == null) return policyRegistry.get("STANDARD");
        FineCalculationStrategy strat = policyRegistry.get(role.trim().toUpperCase());
        return strat != null ? strat : policyRegistry.get("STANDARD");
    }

    public static void setCustomStrategyForRole(String role, FineCalculationStrategy strategy) {
        if (role != null && strategy != null) {
            policyRegistry.put(role.trim().toUpperCase(), strategy);
        }
    }

    public static Map<String, FineCalculationStrategy> getAllPolicies() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(policyRegistry));
    }
}
