package factory;

public class Book implements LibraryResource {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String shelfLocation;
    private boolean isAvailable;

    public Book(String id, String title, String author, String isbn, String shelfLocation) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.shelfLocation = shelfLocation;
        this.isAvailable = true;
    }

    public Book(String id, String title, String author, String isbn, String shelfLocation, boolean isAvailable) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.shelfLocation = shelfLocation;
        this.isAvailable = isAvailable;
    }

    @Override public String getId() { return id; }
    @Override public String getTitle() { return title; }
    @Override public String getResourceType() { return "PHYSICAL_BOOK"; }
    @Override public boolean isBorrowable() { return true; }
    @Override public int getMaxBorrowDays() { return 14; }
    @Override public double getLateFeePerDay() { return 1.50; }
    @Override public boolean isAvailable() { return isAvailable; }
    @Override public void setAvailable(boolean available) { this.isAvailable = available; }

    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getShelfLocation() { return shelfLocation; }

    @Override
    public String getDetails() {
        return String.format("[Physical Book] ID: %s | Title: '%s' | Author: %s | ISBN: %s | Shelf: %s | Status: %s",
                id, title, author, isbn, shelfLocation, (isAvailable ? "Available" : "Checked Out"));
    }

    @Override
    public String toString() {
        return getDetails();
    }
}
