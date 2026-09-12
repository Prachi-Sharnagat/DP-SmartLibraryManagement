package model;

public class EBook {
    private String id;
    private String title;
    private String author;
    private double fileSizeMB;
    private String downloadUrl;

    public EBook(String id, String title, String author, double fileSizeMB, String downloadUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.fileSizeMB = fileSizeMB;
        this.downloadUrl = downloadUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public double getFileSizeMB() { return fileSizeMB; }
    public void setFileSizeMB(double fileSizeMB) { this.fileSizeMB = fileSizeMB; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    @Override
    public String toString() {
        return String.format("EBook[ID=%s, Title='%s', Author='%s', Size=%.1fMB, URL=%s]",
                id, title, author, fileSizeMB, downloadUrl);
    }
}
