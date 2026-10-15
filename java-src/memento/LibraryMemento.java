package memento;

/**
 * Memento Design Pattern marker contract.
 * Encapsulates the internal snapshot state of editable library records.
 */
public interface LibraryMemento {
    long getTimestamp();
    String getDescription();
}
