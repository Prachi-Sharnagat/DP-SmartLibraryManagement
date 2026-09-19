package factory;

import java.util.Map;

public class JournalCreator extends ResourceCreator {

    @Override
    protected LibraryResource createResource(String id, String title, Map<String, Object> params) {
        String publisher = (String) params.getOrDefault("publisher", "Academic Press");
        int volume = params.containsKey("volumeNumber") ? ((Number) params.get("volumeNumber")).intValue() : 1;
        int issue = params.containsKey("issueNumber") ? ((Number) params.get("issueNumber")).intValue() : 1;
        boolean isReferenceOnly = params.containsKey("isReferenceOnly") ? (Boolean) params.get("isReferenceOnly") : false;
        return new Journal(id, title, publisher, volume, issue, isReferenceOnly);
    }
}
