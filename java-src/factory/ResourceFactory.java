package factory;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry and legacy adapter for the GoF Factory Method implementation.
 */
public class ResourceFactory {

    public enum ResourceType {
        BOOK,
        EBOOK,
        JOURNAL
    }

    private static final Map<ResourceType, ResourceCreator> creators = new HashMap<>();

    static {
        creators.put(ResourceType.BOOK, new BookCreator());
        creators.put(ResourceType.EBOOK, new EBookCreator());
        creators.put(ResourceType.JOURNAL, new JournalCreator());
    }

    public static ResourceCreator getCreator(ResourceType type) {
        ResourceCreator creator = creators.get(type);
        if (creator == null) {
            throw new IllegalArgumentException("No creator registered for Resource Type: " + type);
        }
        return creator;
    }

    // Modern GoF Usage via Registry
    public static LibraryResource createResource(ResourceType type, Map<String, Object> params) {
        String id = (String) params.get("id");
        String title = (String) params.get("title");
        return getCreator(type).instantiate(id, title, params);
    }

    // Legacy Support methods (Delegating to GoF Creators)
    public static LibraryResource createBook(String id, String title, String author, String isbn, String shelfLocation) {
        Map<String, Object> params = new HashMap<>();
        params.put("author", author);
        params.put("isbn", isbn);
        params.put("shelfLocation", shelfLocation);
        return getCreator(ResourceType.BOOK).instantiate(id, title, params);
    }

    public static LibraryResource createEBook(String id, String title, String author, double fileSizeMB, String format) {
        Map<String, Object> params = new HashMap<>();
        params.put("author", author);
        params.put("fileSizeMB", fileSizeMB);
        params.put("downloadFormat", format);
        return getCreator(ResourceType.EBOOK).instantiate(id, title, params);
    }

    public static LibraryResource createJournal(String id, String title, String publisher, int volumeNumber, int issueNumber, boolean isReferenceOnly) {
        Map<String, Object> params = new HashMap<>();
        params.put("publisher", publisher);
        params.put("volumeNumber", volumeNumber);
        params.put("issueNumber", issueNumber);
        params.put("isReferenceOnly", isReferenceOnly);
        return getCreator(ResourceType.JOURNAL).instantiate(id, title, params);
    }
}
