package factory;

public interface LibraryResource {
    String getId();
    String getTitle();
    String getResourceType();
    boolean isBorrowable();
    int getMaxBorrowDays();
    double getLateFeePerDay();
    String getDetails();
    void setAvailable(boolean available);
    boolean isAvailable();
}
