package factory;

public class Journal implements LibraryResource {
    private String id;
    private String title;
    private String publisher;
    private int volumeNumber;
    private int issueNumber;
    private boolean isReferenceOnly;
    private boolean isAvailable;

    public Journal(String id, String title, String publisher, int volumeNumber, int issueNumber, boolean isReferenceOnly) {
        this.id = id;
        this.title = title;
        this.publisher = publisher;
        this.volumeNumber = volumeNumber;
        this.issueNumber = issueNumber;
        this.isReferenceOnly = isReferenceOnly;
        this.isAvailable = true;
    }

    public Journal(String id, String title, String publisher, int volumeNumber, int issueNumber, boolean isReferenceOnly, boolean isAvailable) {
        this.id = id;
        this.title = title;
        this.publisher = publisher;
        this.volumeNumber = volumeNumber;
        this.issueNumber = issueNumber;
        this.isReferenceOnly = isReferenceOnly;
        this.isAvailable = isAvailable;
    }

    @Override public String getId() { return id; }
    @Override public String getTitle() { return title; }
    @Override public String getResourceType() { return "JOURNAL"; }
    @Override public boolean isBorrowable() { return !isReferenceOnly; }
    @Override public int getMaxBorrowDays() { return 7; }
    @Override public double getLateFeePerDay() { return 2.50; }
    @Override public boolean isAvailable() { return isAvailable; }
    @Override public void setAvailable(boolean available) { this.isAvailable = available; }

    public String getPublisher() { return publisher; }
    public int getVolumeNumber() { return volumeNumber; }
    public int getIssueNumber() { return issueNumber; }
    public boolean isReferenceOnly() { return isReferenceOnly; }

    @Override
    public String getDetails() {
        return String.format("[Academic Journal] ID: %s | Title: '%s' | Publisher: %s | Vol: %d, Iss: %d | Ref Only: %b | Status: %s",
                id, title, publisher, volumeNumber, issueNumber, isReferenceOnly, (isAvailable ? "Available" : "In Use"));
    }

    @Override
    public String toString() {
        return getDetails();
    }
}
