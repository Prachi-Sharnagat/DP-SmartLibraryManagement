package model;

public class Book {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private boolean isAvailable;
    private String rackLocation;

    public Book(String id, String title, String author, String isbn, boolean isAvailable, String rackLocation) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isAvailable = isAvailable;
        this.rackLocation = rackLocation;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getRackLocation() { return rackLocation; }
    public void setRackLocation(String rackLocation) { this.rackLocation = rackLocation; }

    @Override
    public String toString() {
        return String.format("PhysicalBook[ID=%s, Title='%s', Author='%s', ISBN=%s, Available=%b, Rack=%s]",
                id, title, author, isbn, isAvailable, rackLocation);
    }
}
