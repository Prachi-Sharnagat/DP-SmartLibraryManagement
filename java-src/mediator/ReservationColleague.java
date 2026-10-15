package mediator;

import database.Database;
import model.ReservationRecord;
import observer.ReservationManager;

/**
 * Colleague handling reservation lookup and hold queues.
 * Decoupled from notification dispatching and circulation lifecycle.
 */
public class ReservationColleague extends Colleague {
    private final Database db;
    private final ReservationManager reservationManager;

    public ReservationColleague(Database db, ReservationManager reservationManager) {
        this.db = db;
        this.reservationManager = reservationManager;
    }

    public ReservationRecord getActiveReservation(String resourceId) {
        ReservationRecord record = db.getReservation(resourceId);
        if (record != null && !"CANCELLED".equalsIgnoreCase(record.getStatus()) && !"CLAIMED".equalsIgnoreCase(record.getStatus())) {
            return record;
        }
        return null;
    }

    public boolean hasWaitlistSubscribers(String resourceId) {
        return !reservationManager.getWaitlist(resourceId).isEmpty() || getActiveReservation(resourceId) != null;
    }

    public void markReservationReady(String resourceId) {
        ReservationRecord record = db.getReservation(resourceId);
        if (record != null) {
            record.setStatus("READY_FOR_PICKUP");
            db.addReservation(record);
        }
    }

    public void clearReservation(String resourceId) {
        db.removeReservation(resourceId);
    }
}
