package factory;

import java.util.Map;

public class BookCreator extends ResourceCreator {

    @Override
    protected LibraryResource createResource(String id, String title, Map<String, Object> params) {
        String author = (String) params.getOrDefault("author", "Unknown Author");
        String isbn = (String) params.getOrDefault("isbn", "N/A");
        String shelfLocation = (String) params.getOrDefault("shelfLocation", "General Shelf");
        return new Book(id, title, author, isbn, shelfLocation);
    }
}
