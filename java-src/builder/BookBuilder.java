package builder;

import factory.Book;

/**
 * Builder Design Pattern for constructing Book objects.
 * Enforces mandatory field validation (ID, title, author, ISBN) before instantiation
 * and prevents telescopic constructors with numerous optional parameters.
 */
public class BookBuilder {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String shelfLocation = "General Stacks";
    private boolean isAvailable = true;
    private String publisher = "Standard Publishing";
    private String edition = "1st Edition";
    private String category = "General";
    private int publicationYear = 2024;
    private int totalCopies = 1;
    private int availableCopies = 1;
    private int borrowDays = 14;
    private double finePerDay = 1.50;

    public BookBuilder() {}

    public BookBuilder id(String id) {
        this.id = id;
        return this;
    }

    public BookBuilder title(String title) {
        this.title = title;
        return this;
    }

    public BookBuilder author(String author) {
        this.author = author;
        return this;
    }

    public BookBuilder isbn(String isbn) {
        this.isbn = isbn;
        return this;
    }

    public BookBuilder shelfLocation(String shelfLocation) {
        if (shelfLocation != null && !shelfLocation.trim().isEmpty()) {
            this.shelfLocation = shelfLocation.trim();
        }
        return this;
    }

    public BookBuilder isAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
        return this;
    }

    public BookBuilder publisher(String publisher) {
        if (publisher != null && !publisher.trim().isEmpty()) {
            this.publisher = publisher.trim();
        }
        return this;
    }

    public BookBuilder edition(String edition) {
        if (edition != null && !edition.trim().isEmpty()) {
            this.edition = edition.trim();
        }
        return this;
    }

    public BookBuilder category(String category) {
        if (category != null && !category.trim().isEmpty()) {
            this.category = category.trim();
        }
        return this;
    }

    public BookBuilder publicationYear(int year) {
        if (year > 0) {
            this.publicationYear = year;
        }
        return this;
    }

    public BookBuilder totalCopies(int totalCopies) {
        this.totalCopies = Math.max(1, totalCopies);
        return this;
    }

    public BookBuilder availableCopies(int availableCopies) {
        this.availableCopies = Math.max(0, availableCopies);
        return this;
    }

    public BookBuilder borrowDays(int days) {
        if (days > 0) this.borrowDays = days;
        return this;
    }

    public BookBuilder finePerDay(double fine) {
        if (fine >= 0) this.finePerDay = fine;
        return this;
    }

    /**
     * Builds and returns a validated Book instance.
     *
     * @throws IllegalStateException if any mandatory field is missing or empty
     */
    public Book build() {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalStateException("Validation Error: Book ID is required.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalStateException("Validation Error: Book Title is required.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalStateException("Validation Error: Book Author is required.");
        }
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalStateException("Validation Error: Book ISBN is required.");
        }

        return new Book(
                id.trim(),
                title.trim(),
                author.trim(),
                isbn.trim(),
                shelfLocation,
                isAvailable,
                publisher,
                edition,
                category,
                publicationYear,
                totalCopies,
                availableCopies,
                borrowDays,
                finePerDay
        );
    }
}
