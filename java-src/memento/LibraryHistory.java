package memento;

import database.Database;
import factory.Book;
import factory.LibraryResource;
import model.AuditLogEntry;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Caretaker in the Memento Design Pattern.
 * Manages the history of state snapshots (undo/redo stacks) while preserving complete encapsulation.
 * The Caretaker does not inspect or mutate the internal fields of the memento.
 * Ensures undo executes as a safe, validated compensating database update.
 */
public class LibraryHistory {
    private static LibraryHistory instance;

    private final Deque<ResourceMemento> undoStack = new ConcurrentLinkedDeque<>();
    private final Deque<ResourceMemento> redoStack = new ConcurrentLinkedDeque<>();
    private static final int MAX_HISTORY_SIZE = 50;

    // Set of irreversible business actions that Memento explicitly refuses to undo
    private static final Set<String> IRREVERSIBLE_ACTIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "FINE_PAYMENT",
                    "EXTERNAL_NOTIFICATION_SENT",
                    "SMS_DISPATCHED",
                    "LOAN_PURGED"
            ))
    );

    private LibraryHistory() {}

    public static synchronized LibraryHistory getInstance() {
        if (instance == null) {
            instance = new LibraryHistory();
        }
        return instance;
    }

    /**
     * Records a pre-mutation snapshot into the undo history.
     */
    public synchronized void pushSnapshot(ResourceMemento memento) {
        if (memento == null) return;
        undoStack.push(memento);
        redoStack.clear(); // new edit clears redo branch

        while (undoStack.size() > MAX_HISTORY_SIZE) {
            undoStack.removeLast();
        }
    }

    public synchronized boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public synchronized boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public synchronized int getUndoCount() {
        return undoStack.size();
    }

    /**
     * Restores the most recent supported change as a safe, validated compensating update.
     *
     * @param db Database instance
     * @param callerUserId Librarian triggering the undo
     * @return Restored ResourceMemento or null if history is empty
     */
    public synchronized ResourceMemento undo(Database db, String callerUserId) {
        if (!canUndo()) {
            return null;
        }

        ResourceMemento targetMemento = undoStack.pop();
        String resourceId = targetMemento.getResourceId();

        LibraryResource currentResource = db.getResource(resourceId);
        if (currentResource == null) {
            throw new IllegalStateException("Cannot undo edit: Resource no longer exists in database (ID: " + resourceId + ")");
        }

        if (!(currentResource instanceof Book)) {
            throw new UnsupportedOperationException("Memento rollback is currently supported for Book catalog resources.");
        }

        Book book = (Book) currentResource;

        // Push current state onto redo stack before restoring
        ResourceMemento currentStateMemento = book.createMemento("Pre-undo state for " + book.getTitle());
        redoStack.push(currentStateMemento);

        // Safe, encapsulated state restoration
        book.restore(targetMemento);

        // Compensating Database Persistence
        db.saveResource(book);

        // Record Audit Log for Telemetry
        db.addAuditLog(new AuditLogEntry(
                "AUD-UNDO-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                callerUserId != null ? callerUserId : "SYSTEM",
                "MEMENTO_UNDO",
                String.format("Compensating rollback for Book '%s' (ID: %s) to snapshot [%s]",
                        book.getTitle(), resourceId, targetMemento.getDescription()),
                true,
                5
        ));

        return targetMemento;
    }

    /**
     * Re-applies a previously undone change.
     */
    public synchronized ResourceMemento redo(Database db, String callerUserId) {
        if (!canRedo()) {
            return null;
        }

        ResourceMemento targetMemento = redoStack.pop();
        String resourceId = targetMemento.getResourceId();

        LibraryResource currentResource = db.getResource(resourceId);
        if (currentResource == null || !(currentResource instanceof Book)) {
            throw new IllegalStateException("Cannot redo edit: Resource not found or unsupported.");
        }

        Book book = (Book) currentResource;

        // Push current onto undo stack
        ResourceMemento currentStateMemento = book.createMemento("Pre-redo state for " + book.getTitle());
        undoStack.push(currentStateMemento);

        // Restore redo snapshot
        book.restore(targetMemento);
        db.saveResource(book);

        db.addAuditLog(new AuditLogEntry(
                "AUD-REDO-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                callerUserId != null ? callerUserId : "SYSTEM",
                "MEMENTO_REDO",
                String.format("Redo re-applied for Book '%s' (ID: %s)", book.getTitle(), resourceId),
                true,
                5
        ));

        return targetMemento;
    }

    /**
     * Guards against attempting to undo irreversible operations such as financial settlements
     * or external messaging dispatches.
     */
    public static boolean isActionReversible(String actionType) {
        if (actionType == null) return true;
        return !IRREVERSIBLE_ACTIONS.contains(actionType.toUpperCase().trim());
    }

    public static void validateReversibility(String actionType) {
        if (!isActionReversible(actionType)) {
            throw new UnsupportedOperationException(
                    "Irreversible Action: Operation '" + actionType + "' cannot be rolled back via Memento."
            );
        }
    }

    /**
     * Returns an unmodifiable list of current snapshot descriptions in undo history.
     */
    public synchronized List<ResourceMemento> getUndoHistory() {
        return Collections.unmodifiableList(new ArrayList<>(undoStack));
    }
}
