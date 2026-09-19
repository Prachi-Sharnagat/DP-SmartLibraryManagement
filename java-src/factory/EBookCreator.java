package factory;

import java.util.Map;

public class EBookCreator extends ResourceCreator {

    @Override
    protected LibraryResource createResource(String id, String title, Map<String, Object> params) {
        String author = (String) params.getOrDefault("author", "Unknown Author");
        double size = params.containsKey("fileSizeMB") ? ((Number) params.get("fileSizeMB")).doubleValue() : 5.0;
        String format = (String) params.getOrDefault("downloadFormat", "PDF");
        return new EBook(id, title, author, size, format);
    }
}
