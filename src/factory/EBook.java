package factory;

public class EBook implements LibraryResource {
    private String id;
    private String title;
    private String author;
    private double fileSizeMB;
    private String downloadFormat; // PDF, EPUB
    private boolean isAvailable;

    public EBook(String id, String title, String author, double fileSizeMB, String downloadFormat) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.fileSizeMB = fileSizeMB;
        this.downloadFormat = downloadFormat;
        this.isAvailable = true; // Digital assets generally unlimited or simultaneous access
    }

    public EBook(String id, String title, String author, double fileSizeMB, String downloadFormat, boolean isAvailable) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.fileSizeMB = fileSizeMB;
        this.downloadFormat = downloadFormat;
        this.isAvailable = isAvailable;
    }

    @Override public String getId() { return id; }
    @Override public String getTitle() { return title; }
    @Override public String getResourceType() { return "EBOOK"; }
    @Override public boolean isBorrowable() { return true; }
    @Override public int getMaxBorrowDays() { return 21; }
    @Override public double getLateFeePerDay() { return 0.00; } // Auto-expires, no late fee
    @Override public boolean isAvailable() { return isAvailable; }
    @Override public void setAvailable(boolean available) { this.isAvailable = available; }

    public String getAuthor() { return author; }
    public double getFileSizeMB() { return fileSizeMB; }
    public String getDownloadFormat() { return downloadFormat; }

    @Override
    public String getDetails() {
        return String.format("[E-Book (Digital)] ID: %s | Title: '%s' | Author: %s | Size: %.1f MB | Format: %s | DRM Status: %s",
                id, title, author, fileSizeMB, downloadFormat, (isAvailable ? "Active/Licensed" : "License Expired"));
    }

    @Override
    public String toString() {
        return getDetails();
    }
}
