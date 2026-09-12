package model;

public class Journal {
    private String id;
    private String title;
    private String publisher;
    private int volumeNumber;
    private int issueNumber;

    public Journal(String id, String title, String publisher, int volumeNumber, int issueNumber) {
        this.id = id;
        this.title = title;
        this.publisher = publisher;
        this.volumeNumber = volumeNumber;
        this.issueNumber = issueNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public int getVolumeNumber() { return volumeNumber; }
    public void setVolumeNumber(int volumeNumber) { this.volumeNumber = volumeNumber; }

    public int getIssueNumber() { return issueNumber; }
    public void setIssueNumber(int issueNumber) { this.issueNumber = issueNumber; }

    @Override
    public String toString() {
        return String.format("Journal[ID=%s, Title='%s', Publisher='%s', Vol=%d, Issue=%d]",
                id, title, publisher, volumeNumber, issueNumber);
    }
}
