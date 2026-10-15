package builder;

import model.User;
import service.AuthService;

import java.time.LocalDate;

/**
 * Builder Design Pattern for constructing Member / User objects.
 * Validates mandatory identification and contact fields before building.
 */
public class UserBuilder {
    private String userId;
    private String name;
    private String email;
    private String phone = "555-0100";
    private boolean membershipActive = true;
    private double outstandingFine = 0.0;
    private int currentBorrowedCount = 0;
    private String role = "MEMBER";
    private int maxLoans = 5;
    private String department = "General";
    private String semester = "N/A";
    private String joinedDate = LocalDate.now().toString();
    private String avatar = "";
    private String passwordHash;

    public UserBuilder() {
        this.passwordHash = AuthService.hashPassword("password123");
    }

    public UserBuilder userId(String userId) {
        this.userId = userId;
        return this;
    }

    public UserBuilder name(String name) {
        this.name = name;
        return this;
    }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder phone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            this.phone = phone.trim();
        }
        return this;
    }

    public UserBuilder membershipActive(boolean active) {
        this.membershipActive = active;
        return this;
    }

    public UserBuilder outstandingFine(double fine) {
        this.outstandingFine = Math.max(0.0, fine);
        return this;
    }

    public UserBuilder currentBorrowedCount(int count) {
        this.currentBorrowedCount = Math.max(0, count);
        return this;
    }

    public UserBuilder role(String role) {
        if (role != null && !role.trim().isEmpty()) {
            this.role = role.trim().toUpperCase();
        }
        return this;
    }

    public UserBuilder maxLoans(int maxLoans) {
        this.maxLoans = Math.max(1, maxLoans);
        return this;
    }

    public UserBuilder department(String department) {
        if (department != null && !department.trim().isEmpty()) {
            this.department = department.trim();
        }
        return this;
    }

    public UserBuilder semester(String semester) {
        if (semester != null && !semester.trim().isEmpty()) {
            this.semester = semester.trim();
        }
        return this;
    }

    public UserBuilder joinedDate(String joinedDate) {
        if (joinedDate != null && !joinedDate.trim().isEmpty()) {
            this.joinedDate = joinedDate.trim();
        }
        return this;
    }

    public UserBuilder avatar(String avatar) {
        if (avatar != null) {
            this.avatar = avatar;
        }
        return this;
    }

    public UserBuilder passwordHash(String passwordHash) {
        if (passwordHash != null) {
            this.passwordHash = passwordHash;
        }
        return this;
    }

    /**
     * Builds and returns a validated User instance.
     *
     * @throws IllegalStateException if any mandatory field is missing or invalid
     */
    public User build() {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalStateException("Validation Error: User ID is required.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Validation Error: User Name is required.");
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new IllegalStateException("Validation Error: Valid User Email is required.");
        }

        User user = new User(
                userId.trim(),
                name.trim(),
                email.trim(),
                phone,
                membershipActive,
                outstandingFine,
                currentBorrowedCount,
                role
        );
        user.setMaxLoans(maxLoans);
        user.setDepartment(department);
        user.setSemester(semester);
        user.setJoinedDate(joinedDate);
        user.setAvatar(avatar);
        user.setPasswordHash(passwordHash);

        return user;
    }
}
