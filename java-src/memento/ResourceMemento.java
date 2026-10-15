package memento;

import java.io.Serializable;

/**
 * Concrete Memento capturing an immutable snapshot of a Book/Resource editable state.
 * State is encapsulated: internal fields are accessed only through authorized entity restoration.
 */
public class ResourceMemento implements LibraryMemento, Serializable {
    private final String resourceId;
    private final String title;
    private final String author;
    private final String isbn;
    private final String shelfLocation;
    private final String publisher;
    private final String edition;
    private final String category;
    private final int publicationYear;
    private final boolean isAvailable;
    private final int totalCopies;
    private final int availableCopies;
    private final long timestamp;
    private final String description;

    public ResourceMemento(String resourceId, String title, String author, String isbn,
                           String shelfLocation, String publisher, String edition,
                           String category, int publicationYear, boolean isAvailable,
                           int totalCopies, int availableCopies, String description) {
        this.resourceId = resourceId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.shelfLocation = shelfLocation;
        this.publisher = publisher;
        this.edition = edition;
        this.category = category;
        this.publicationYear = publicationYear;
        this.isAvailable = isAvailable;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.timestamp = System.currentTimeMillis();
        this.description = description != null ? description : "Snapshot of resource " + resourceId;
    }

    public String getResourceId() { return resourceId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getShelfLocation() { return shelfLocation; }
    public String getPublisher() { return publisher; }
    public String getEdition() { return edition; }
    public String getCategory() { return category; }
    public int getPublicationYear() { return publicationYear; }
    public boolean isAvailable() { return isAvailable; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    @Override
    public long getTimestamp() { return timestamp; }

    @Override
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("ResourceMemento[ID=%s, Title='%s', Author='%s', Shelf='%s', Timestamp=%d, Desc='%s']",
                resourceId, title, author, shelfLocation, timestamp, description);
    }
}
