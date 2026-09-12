package model;

import java.time.LocalDate;

public class Loan {
    private String loanId;
    private String userId;
    private String resourceId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fine;
    private boolean isReturned;

    public Loan(String loanId, String userId, String resourceId, LocalDate borrowDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.userId = userId;
        this.resourceId = resourceId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.fine = 0.0;
        this.isReturned = false;
    }

    public Loan(String loanId, String userId, String resourceId, LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate, double fine, boolean isReturned) {
        this.loanId = loanId;
        this.userId = userId;
        this.resourceId = resourceId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fine = fine;
        this.isReturned = isReturned;
    }

    public String getLoanId() { return loanId; }
    public String getUserId() { return userId; }
    public String getResourceId() { return resourceId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public double getFine() { return fine; }
    public void setFine(double fine) { this.fine = fine; }
    public boolean isReturned() { return isReturned; }
    public void setReturned(boolean returned) { isReturned = returned; }

    @Override
    public String toString() {
        return String.format("Loan[ID=%s, User=%s, Resource=%s, Borrowed=%s, Due=%s, Returned=%s, Fine=$%.2f]",
                loanId, userId, resourceId, borrowDate, dueDate, (isReturned ? returnDate.toString() : "ACTIVE"), fine);
    }
}
