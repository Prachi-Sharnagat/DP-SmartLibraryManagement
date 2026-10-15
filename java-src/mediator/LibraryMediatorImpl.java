package mediator;

import database.Database;
import factory.LibraryResource;
import model.Loan;
import model.ReservationRecord;
import observer.ReservationManager;
import strategy.FineCalculationResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Mediator coordinating the library circulation lifecycle.
 * Decouples Circulation, Reservation, Fine, and Notification services,
 * eliminating peer-to-peer coupling and preventing infinite recursive loops.
 */
public class LibraryMediatorImpl implements LibraryMediator {
    private static LibraryMediatorImpl instance;

    private CirculationColleague circulationColleague;
    private ReservationColleague reservationColleague;
    private FineColleague fineColleague;
    private NotificationColleague notificationColleague;

    // Protection flag to prevent recursive coordination loops
    private final ThreadLocal<Boolean> isCoordinating = ThreadLocal.withInitial(() -> false);

    public LibraryMediatorImpl() {
        Database db = Database.getInstance();
        ReservationManager rm = ReservationManager.getInstance();

        this.circulationColleague = new CirculationColleague(db);
        this.reservationColleague = new ReservationColleague(db, rm);
        this.fineColleague = new FineColleague(db);
        this.notificationColleague = new NotificationColleague(db, rm);

        this.circulationColleague.setMediator(this);
        this.reservationColleague.setMediator(this);
        this.fineColleague.setMediator(this);
        this.notificationColleague.setMediator(this);
    }

    public static synchronized LibraryMediatorImpl getInstance() {
        if (instance == null) {
            instance = new LibraryMediatorImpl();
        }
        return instance;
    }

    @Override
    public void registerCirculationColleague(CirculationColleague colleague) {
        this.circulationColleague = colleague;
        if (colleague != null) colleague.setMediator(this);
    }

    @Override
    public void registerReservationColleague(ReservationColleague colleague) {
        this.reservationColleague = colleague;
        if (colleague != null) colleague.setMediator(this);
    }

    @Override
    public void registerFineColleague(FineColleague colleague) {
        this.fineColleague = colleague;
        if (colleague != null) colleague.setMediator(this);
    }

    @Override
    public void registerNotificationColleague(NotificationColleague colleague) {
        this.notificationColleague = colleague;
        if (colleague != null) colleague.setMediator(this);
    }

    @Override
    public ReturnCoordinationResult coordinateReturn(String callerUserId, String resourceId) {
        if (isCoordinating.get()) {
            throw new IllegalStateException("Recursive circulation coordination detected and blocked by LibraryMediator.");
        }

        long start = System.currentTimeMillis();
        List<String> log = new ArrayList<>();
        isCoordinating.set(true);

        try {
            log.add("LibraryMediator: Initialized return workflow for Resource " + resourceId);

            // 1. Fetch resource and active loan
            LibraryResource resource = circulationColleague.getResource(resourceId);
            if (resource == null) {
                log.add("CirculationColleague: Resource not found with ID " + resourceId);
                return new ReturnCoordinationResult(false, "Resource not found: " + resourceId, null, null, false, null, log, System.currentTimeMillis() - start);
            }

            Loan activeLoan = circulationColleague.getActiveLoanForResource(resourceId);
            String borrowerId = activeLoan != null ? activeLoan.getUserId() : callerUserId;
            LocalDate dueDate = activeLoan != null ? activeLoan.getDueDate() : LocalDate.now();
            LocalDate returnDate = LocalDate.now();

            log.add("CirculationColleague: Active loan found (" + (activeLoan != null ? activeLoan.getLoanId() : "NONE") + ") for borrower " + borrowerId);

            // 2. Coordinate with FineColleague (Strategy Pattern)
            FineCalculationResult fineResult = fineColleague.assessReturnFine(borrowerId, dueDate, returnDate);
            log.add("FineColleague: Strategy assessed " + fineResult.getSummary());

            // 3. Coordinate with CirculationColleague to complete loan
            if (activeLoan != null) {
                circulationColleague.completeLoanReturn(activeLoan, returnDate, fineResult.getCalculatedFine());
                log.add("CirculationColleague: Closed loan " + activeLoan.getLoanId() + " and decremented borrower's loan count");
            }

            // 4. Coordinate with ReservationColleague
            ReservationRecord reservation = reservationColleague.getActiveReservation(resourceId);
            boolean isHeld = false;
            String nextReservedUser = null;

            if (reservation != null) {
                isHeld = true;
                nextReservedUser = reservation.getUserId();
                reservationColleague.markReservationReady(resourceId);
                // Keep unavailable as it is held exclusively for reserving patron
                circulationColleague.updateResourceAvailability(resource, false);
                log.add("ReservationColleague: Found active reservation for " + nextReservedUser + ". Resource held on pickup shelf.");

                // 5. Coordinate with NotificationColleague for waitlist
                notificationColleague.notifyWaitlistHold(resource, nextReservedUser);
                log.add("NotificationColleague: Alerted reserving patron " + nextReservedUser + " via multi-channel notification");
            } else {
                // Return to general catalog inventory
                circulationColleague.updateResourceAvailability(resource, true);
                log.add("CirculationColleague: Resource marked available for general checkout");
            }

            // 6. Coordinate return receipt to the returning patron
            notificationColleague.dispatchReturnReceipt(borrowerId, resource, fineResult.getCalculatedFine());
            log.add("NotificationColleague: Dispatched return confirmation receipt to " + borrowerId);

            long latency = System.currentTimeMillis() - start;
            String message = String.format("Return processed successfully. Fine: $%.2f%s",
                    fineResult.getCalculatedFine(),
                    (isHeld ? " (Held for reserved patron: " + nextReservedUser + ")" : ""));

            return new ReturnCoordinationResult(true, message, activeLoan, fineResult, isHeld, nextReservedUser, log, latency);

        } finally {
            isCoordinating.set(false);
        }
    }
}
