package factory;

import memento.ResourceMemento;
import prototype.Prototype;

public class Book implements LibraryResource, Prototype<Book> {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String shelfLocation;
    private boolean isAvailable;

    // Optional metadata fields supported via Builder, Prototype & Memento
    private String publisher;
    private String edition;
    private String category;
    private int publicationYear;
    private int totalCopies;
    private int availableCopies;
    private int borrowDays;
    private double finePerDay;

    // Backward-compatible constructor
    public Book(String id, String title, String author, String isbn, String shelfLocation) {
        this(id, title, author, isbn, shelfLocation, true, "Standard Publishing", "1st Edition", "General", 2024, 1, 1, 14, 1.50);
    }

    // Backward-compatible constructor with availability
    public Book(String id, String title, String author, String isbn, String shelfLocation, boolean isAvailable) {
        this(id, title, author, isbn, shelfLocation, isAvailable, "Standard Publishing", "1st Edition", "General", 2024, 1, (isAvailable ? 1 : 0), 14, 1.50);
    }

    // Comprehensive constructor used by BookBuilder & Prototype
    public Book(String id, String title, String author, String isbn, String shelfLocation, boolean isAvailable,
                String publisher, String edition, String category, int publicationYear,
                int totalCopies, int availableCopies, int borrowDays, double finePerDay) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.shelfLocation = shelfLocation != null ? shelfLocation : "General Stacks";
        this.isAvailable = isAvailable;
        this.publisher = publisher != null ? publisher : "Standard Publishing";
        this.edition = edition != null ? edition : "1st Edition";
        this.category = category != null ? category : "General";
        this.publicationYear = publicationYear > 0 ? publicationYear : 2024;
        this.totalCopies = Math.max(1, totalCopies);
        this.availableCopies = Math.max(0, availableCopies);
        this.borrowDays = borrowDays > 0 ? borrowDays : 14;
        this.finePerDay = finePerDay >= 0 ? finePerDay : 1.50;
    }

    @Override public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Override public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @Override public String getResourceType() { return "PHYSICAL_BOOK"; }
    @Override public boolean isBorrowable() { return true; }
    @Override public int getMaxBorrowDays() { return borrowDays; }
    @Override public double getLateFeePerDay() { return finePerDay; }
    @Override public boolean isAvailable() { return isAvailable; }
    @Override public void setAvailable(boolean available) {
        this.isAvailable = available;
        if (!available && this.availableCopies > 0) {
            this.availableCopies--;
        } else if (available && this.availableCopies < this.totalCopies) {
            this.availableCopies++;
        }
    }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getPublicationYear() { return publicationYear; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public void setBorrowDays(int borrowDays) { this.borrowDays = borrowDays; }
    public void setFinePerDay(double finePerDay) { this.finePerDay = finePerDay; }

    // ==========================================
    // PROTOTYPE DESIGN PATTERN IMPLEMENTATION
    // ==========================================

    /**
     * Clones the reusable catalog metadata of this Book.
     */
    @Override
    public Book clone() {
        return new Book(
                this.id,
                this.title,
                this.author,
                this.isbn,
                this.shelfLocation,
                this.isAvailable,
                this.publisher,
                this.edition,
                this.category,
                this.publicationYear,
                this.totalCopies,
                this.availableCopies,
                this.borrowDays,
                this.finePerDay
        );
    }

    /**
     * Creates a new, distinct physical copy record from this book template/record.
     * Guarantees a unique copy ID, independent mutable status, and separate shelf location.
     */
    public Book createCopy(String newCopyId, String newShelfLocation) {
        if (newCopyId == null || newCopyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Copy ID cannot be null or empty.");
        }
        if (newCopyId.trim().equalsIgnoreCase(this.id)) {
            throw new IllegalArgumentException("New physical copy ID (" + newCopyId + ") must differ from template ID (" + this.id + ").");
        }
        String shelf = (newShelfLocation != null && !newShelfLocation.trim().isEmpty()) ? newShelfLocation.trim() : this.shelfLocation;

        return new Book(
                newCopyId.trim(),
                this.title,
                this.author,
                this.isbn,
                shelf,
                true, // fresh independent copy is immediately available
                this.publisher,
                this.edition,
                this.category,
                this.publicationYear,
                1,
                1,
                this.borrowDays,
                this.finePerDay
        );
    }

    // ==========================================
    // MEMENTO DESIGN PATTERN IMPLEMENTATION
    // ==========================================

    /**
     * Captures current editable state into an encapsulated Memento.
     */
    public ResourceMemento createMemento(String description) {
        return new ResourceMemento(
                this.id,
                this.title,
                this.author,
                this.isbn,
                this.shelfLocation,
                this.publisher,
                this.edition,
                this.category,
                this.publicationYear,
                this.isAvailable,
                this.totalCopies,
                this.availableCopies,
                description
        );
    }

    /**
     * Restores state from a validated Memento.
     */
    public void restore(ResourceMemento memento) {
        if (memento == null) {
            throw new IllegalArgumentException("Cannot restore from null memento.");
        }
        if (!memento.getResourceId().equals(this.id)) {
            throw new IllegalStateException(String.format("Memento target resource ID mismatch: expected %s but got %s",
                    this.id, memento.getResourceId()));
        }
        this.title = memento.getTitle();
        this.author = memento.getAuthor();
        this.isbn = memento.getIsbn();
        this.shelfLocation = memento.getShelfLocation();
        this.publisher = memento.getPublisher();
        this.edition = memento.getEdition();
        this.category = memento.getCategory();
        this.publicationYear = memento.getPublicationYear();
        this.isAvailable = memento.isAvailable();
        this.totalCopies = memento.getTotalCopies();
        this.availableCopies = memento.getAvailableCopies();
    }

    @Override
    public String getDetails() {
        return String.format("[Physical Book] ID: %s | Title: '%s' | Author: %s | ISBN: %s | Category: %s | Edition: %s (%d) | Shelf: %s | Status: %s",
                id, title, author, isbn, category, edition, publicationYear, shelfLocation, (isAvailable ? "Available" : "Checked Out"));
    }

    @Override
    public String toString() {
        return getDetails();
    }
}
