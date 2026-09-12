package model;

public class User {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private boolean membershipActive;
    private double outstandingFine;
    private int currentBorrowedCount;
    private String role; // "MEMBER", "LIBRARIAN", "ADMIN"

    public User(String userId, String name, String email, String phone, boolean membershipActive, double outstandingFine, int currentBorrowedCount, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.membershipActive = membershipActive;
        this.outstandingFine = outstandingFine;
        this.currentBorrowedCount = currentBorrowedCount;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isMembershipActive() { return membershipActive; }
    public void setMembershipActive(boolean membershipActive) { this.membershipActive = membershipActive; }

    public double getOutstandingFine() { return outstandingFine; }
    public void setOutstandingFine(double outstandingFine) { this.outstandingFine = outstandingFine; }

    public int getCurrentBorrowedCount() { return currentBorrowedCount; }
    public void setCurrentBorrowedCount(int currentBorrowedCount) { this.currentBorrowedCount = currentBorrowedCount; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return String.format("User[ID=%s, Name=%s, Email=%s, Active=%b, Fine=$%.2f, Borrowed=%d, Role=%s]",
                userId, name, email, membershipActive, outstandingFine, currentBorrowedCount, role);
    }
}
