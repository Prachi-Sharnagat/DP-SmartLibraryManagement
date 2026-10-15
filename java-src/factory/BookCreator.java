package factory;

import builder.BookBuilder;
import java.util.Map;

/**
 * Concrete Creator in Factory Method pattern.
 * Delegates complex Book assembly to the BookBuilder pattern with validation.
 */
public class BookCreator extends ResourceCreator {

    @Override
    protected LibraryResource createResource(String id, String title, Map<String, Object> params) {
        String author = (String) params.getOrDefault("author", "Unknown Author");
        String isbn = (String) params.getOrDefault("isbn", "978-0000000000");
        String shelfLocation = (String) params.getOrDefault("shelfLocation", "General Stacks");
        String publisher = (String) params.getOrDefault("publisher", "Standard Publishing");
        String edition = (String) params.getOrDefault("edition", "1st Edition");
        String category = (String) params.getOrDefault("category", "General");
        int year = params.containsKey("publicationYear") ? ((Number) params.get("publicationYear")).intValue() : 2024;

        return new BookBuilder()
                .id(id)
                .title(title)
                .author(author)
                .isbn(isbn)
                .shelfLocation(shelfLocation)
                .publisher(publisher)
                .edition(edition)
                .category(category)
                .publicationYear(year)
                .build();
    }
}
