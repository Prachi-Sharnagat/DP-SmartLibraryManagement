package mediator;

import bridge.*;
import database.Database;
import factory.LibraryResource;
import model.NotificationRecord;
import model.User;
import observer.ReservationManager;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Colleague handling multi-channel notification dispatching and Observer waitlist broadcasts.
 * Integrates Bridge and Observer patterns without circular dependencies.
 */
public class NotificationColleague extends Colleague {
    private final Database db;
    private final ReservationManager reservationManager;

    public NotificationColleague(Database db, ReservationManager reservationManager) {
        this.db = db;
        this.reservationManager = reservationManager;
    }

    public void dispatchReturnReceipt(String borrowerId, LibraryResource resource, double fineAmount) {
        User user = db.getUser(borrowerId);
        String userName = user != null ? user.getName() : "Valued Member";
        String title = resource != null ? resource.getTitle() : "Library Item";

        String msg = String.format(Locale.US,
                "Return receipt for '%s' processed successfully.%s",
                title, (fineAmount > 0.0 ? String.format(Locale.US, " Assessed overdue fine: $%.2f.", fineAmount) : " No overdue fines assessed."));

        NotificationRecord record = new NotificationRecord(
                "NOTIF-RET-" + System.currentTimeMillis() % 100000,
                borrowerId,
                userName,
                "IN_APP",
                "Item Returned Successfully",
                msg,
                "DELIVERED"
        );
        db.addNotification(record);
    }

    public void notifyWaitlistHold(LibraryResource resource, String targetUserId) {
        String resId = resource != null ? resource.getId() : "N/A";
        String title = resource != null ? resource.getTitle() : "Reserved Item";

        // Broadcast to Observer waitlist subscribers
        reservationManager.notifyObservers(resId, title, "RESOURCE_AVAILABLE",
                "Your reserved book '" + title + "' is now ready for pickup at circulation desk.");

        // Direct in-app record for the specific reserving user
        if (targetUserId != null) {
            User reservingUser = db.getUser(targetUserId);
            String name = reservingUser != null ? reservingUser.getName() : "Reserving Member";
            NotificationRecord record = new NotificationRecord(
                    "NOTIF-HOLD-" + System.currentTimeMillis() % 100000,
                    targetUserId,
                    name,
                    "IN_APP",
                    "Reserved Book Ready for Pickup",
                    "The book '" + title + "' you reserved is now on hold for you.",
                    "DELIVERED"
            );
            db.addNotification(record);
        }
    }
}
