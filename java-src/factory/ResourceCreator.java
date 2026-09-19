package factory;

import java.util.Map;

/**
 * The GoF Factory Method Abstract Creator.
 * Declares the factory method, which returns an object of type LibraryResource.
 */
public abstract class ResourceCreator {

    /**
     * The Factory Method.
     * Subclasses override this to instantiate the specific concrete resource.
     */
    protected abstract LibraryResource createResource(String id, String title, Map<String, Object> params);

    /**
     * Core operation that relies on the factory method.
     */
    public LibraryResource instantiate(String id, String title, Map<String, Object> params) {
        LibraryResource resource = createResource(id, title, params);
        // Can add common initialization logic here if needed
        return resource;
    }
}
