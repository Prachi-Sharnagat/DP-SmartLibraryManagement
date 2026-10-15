package prototype;

import factory.Book;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prototype Registry storing reusable catalog templates for quick duplication of book metadata.
 */
public class CatalogTemplateRegistry {
    private static CatalogTemplateRegistry instance;
    private final Map<String, Book> templates = new ConcurrentHashMap<>();

    private CatalogTemplateRegistry() {
        initDefaultTemplates();
    }

    public static synchronized CatalogTemplateRegistry getInstance() {
        if (instance == null) {
            instance = new CatalogTemplateRegistry();
        }
        return instance;
    }

    private void initDefaultTemplates() {
        Book csTemplate = new Book(
                "TEMPLATE-CS",
                "Computer Science Core Curriculum Textbook",
                "Faculty Editorial Board",
                "978-0130000001",
                "Stacks-CS-Core",
                true,
                "MIT Press",
                "Revised Standard Edition",
                "Computer Science",
                2024,
                1,
                1,
                14,
                1.50
        );

        Book refTemplate = new Book(
                "TEMPLATE-REF",
                "Comprehensive Engineering Handbook",
                "IEEE Academic Syndicate",
                "978-0470000002",
                "Reference-Wing-East",
                true,
                "Wiley-IEEE Press",
                "Reference Edition",
                "Engineering",
                2023,
                1,
                1,
                3,
                2.50
        );

        Book litTemplate = new Book(
                "TEMPLATE-LIT",
                "World Literature & Anthology Reader",
                "Oxford Literary Guild",
                "978-0190000003",
                "Stacks-Humanities-01",
                true,
                "Oxford University Press",
                "Critical Edition",
                "Literature",
                2022,
                1,
                1,
                21,
                0.75
        );

        templates.put("CS_TEXTBOOK", csTemplate);
        templates.put("REFERENCE_MANUAL", refTemplate);
        templates.put("CLASSIC_LITERATURE", litTemplate);
    }

    public void registerTemplate(String templateKey, Book template) {
        if (templateKey != null && template != null) {
            templates.put(templateKey.trim().toUpperCase(), template);
        }
    }

    public Book getTemplate(String templateKey) {
        if (templateKey == null) return null;
        Book template = templates.get(templateKey.trim().toUpperCase());
        return template != null ? template.clone() : null;
    }

    /**
     * Clones metadata from an existing template into a new book record with unique identity.
     */
    public Book cloneFromTemplate(String templateKey, String newId, String newTitle, String newIsbn) {
        Book template = getTemplate(templateKey);
        if (template == null) {
            throw new IllegalArgumentException("Catalog template not found for key: " + templateKey);
        }
        if (newId == null || newId.trim().isEmpty()) {
            throw new IllegalArgumentException("New book ID is required when cloning from template.");
        }

        template.setId(newId.trim());
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            template.setTitle(newTitle.trim());
        }
        if (newIsbn != null && !newIsbn.trim().isEmpty()) {
            template.setIsbn(newIsbn.trim());
        }
        template.setAvailable(true);
        return template;
    }

    public Map<String, Book> getAllTemplates() {
        return new ConcurrentHashMap<>(templates);
    }
}
