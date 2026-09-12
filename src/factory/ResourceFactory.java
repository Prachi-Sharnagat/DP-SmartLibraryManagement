package factory;

import java.util.Map;

/**
 * Factory Pattern Implementation for creating Library Resources.
 */
public class ResourceFactory {

    public enum ResourceType {
        BOOK,
        EBOOK,
        JOURNAL
    }

    public static LibraryResource createResource(ResourceType type, Map<String, Object> params) {
        String id = (String) params.get("id");
        String title = (String) params.get("title");

        switch (type) {
            case BOOK:
                String author = (String) params.getOrDefault("author", "Unknown Author");
                String isbn = (String) params.getOrDefault("isbn", "N/A");
                String shelfLocation = (String) params.getOrDefault("shelfLocation", "General Shelf");
                return new Book(id, title, author, isbn, shelfLocation);

            case EBOOK:
                String ebookAuthor = (String) params.getOrDefault("author", "Unknown Author");
                double size = params.containsKey("fileSizeMB") ? ((Number) params.get("fileSizeMB")).doubleValue() : 5.0;
                String format = (String) params.getOrDefault("downloadFormat", "PDF");
                return new EBook(id, title, ebookAuthor, size, format);

            case JOURNAL:
                String publisher = (String) params.getOrDefault("publisher", "Academic Press");
                int volume = params.containsKey("volumeNumber") ? ((Number) params.get("volumeNumber")).intValue() : 1;
                int issue = params.containsKey("issueNumber") ? ((Number) params.get("issueNumber")).intValue() : 1;
                boolean isReferenceOnly = params.containsKey("isReferenceOnly") ? (Boolean) params.get("isReferenceOnly") : false;
                return new Journal(id, title, publisher, volume, issue, isReferenceOnly);

            default:
                throw new IllegalArgumentException("Unknown Resource Type: " + type);
        }
    }

    public static LibraryResource createBook(String id, String title, String author, String isbn, String shelfLocation) {
        return new Book(id, title, author, isbn, shelfLocation);
    }

    public static LibraryResource createEBook(String id, String title, String author, double fileSizeMB, String format) {
        return new EBook(id, title, author, fileSizeMB, format);
    }

    public static LibraryResource createJournal(String id, String title, String publisher, int volumeNumber, int issueNumber, boolean isReferenceOnly) {
        return new Journal(id, title, publisher, volumeNumber, issueNumber, isReferenceOnly);
    }
}
