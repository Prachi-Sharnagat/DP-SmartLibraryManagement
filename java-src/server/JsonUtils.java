package server;

import factory.Book;
import factory.EBook;
import factory.Journal;
import factory.LibraryResource;
import model.Loan;
import model.User;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight JSON utility for zero-dependency serialization and parsing in standard Java.
 */
public class JsonUtils {

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String userToJson(User user) {
        if (user == null) return "null";
        return String.format(Locale.US,
                "{\"userId\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"membershipActive\":%b,\"outstandingFine\":%.2f,\"currentBorrowedCount\":%d,\"role\":\"%s\"}",
                escape(user.getUserId()),
                escape(user.getName()),
                escape(user.getEmail()),
                escape(user.getPhone()),
                user.isMembershipActive(),
                user.getOutstandingFine(),
                user.getCurrentBorrowedCount(),
                escape(user.getRole()));
    }

    public static String resourceToJson(LibraryResource res) {
        if (res == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(escape(res.getId())).append("\",");
        sb.append("\"title\":\"").append(escape(res.getTitle())).append("\",");
        sb.append("\"type\":\"").append(escape(res.getResourceType())).append("\",");
        sb.append("\"isAvailable\":").append(res.isAvailable()).append(",");
        sb.append("\"isBorrowable\":").append(res.isBorrowable()).append(",");
        sb.append("\"maxBorrowDays\":").append(res.getMaxBorrowDays()).append(",");
        sb.append("\"lateFeePerDay\":").append(res.getLateFeePerDay());

        if (res instanceof Book) {
            Book b = (Book) res;
            sb.append(",\"author\":\"").append(escape(b.getAuthor())).append("\"");
            sb.append(",\"isbn\":\"").append(escape(b.getIsbn())).append("\"");
            sb.append(",\"shelfLocation\":\"").append(escape(b.getShelfLocation())).append("\"");
            sb.append(",\"publisher\":\"").append(escape(b.getPublisher())).append("\"");
            sb.append(",\"edition\":\"").append(escape(b.getEdition())).append("\"");
            sb.append(",\"category\":\"").append(escape(b.getCategory())).append("\"");
            sb.append(",\"publicationYear\":").append(b.getPublicationYear());
            sb.append(",\"totalCopies\":").append(b.getTotalCopies());
            sb.append(",\"availableCopies\":").append(b.getAvailableCopies());
            sb.append(",\"isReferenceOnly\":false");
        } else if (res instanceof EBook) {
            EBook eb = (EBook) res;
            sb.append(",\"author\":\"").append(escape(eb.getAuthor())).append("\"");
            sb.append(",\"fileSizeMB\":").append(eb.getFileSizeMB());
            sb.append(",\"downloadFormat\":\"").append(escape(eb.getDownloadFormat())).append("\"");
            sb.append(",\"isReferenceOnly\":false");
        } else if (res instanceof Journal) {
            Journal j = (Journal) res;
            sb.append(",\"publisher\":\"").append(escape(j.getPublisher())).append("\"");
            sb.append(",\"volume\":").append(j.getVolumeNumber());
            sb.append(",\"issue\":").append(j.getIssueNumber());
            sb.append(",\"isReferenceOnly\":").append(j.isReferenceOnly());
        }
        sb.append("}");
        return sb.toString();
    }

    public static String mementoToJson(memento.ResourceMemento m) {
        if (m == null) return "null";
        return String.format(Locale.US,
                "{\"resourceId\":\"%s\",\"title\":\"%s\",\"author\":\"%s\",\"isbn\":\"%s\",\"shelfLocation\":\"%s\",\"publisher\":\"%s\",\"edition\":\"%s\",\"category\":\"%s\",\"publicationYear\":%d,\"timestamp\":%d,\"description\":\"%s\"}",
                escape(m.getResourceId()), escape(m.getTitle()), escape(m.getAuthor()),
                escape(m.getIsbn()), escape(m.getShelfLocation()), escape(m.getPublisher()),
                escape(m.getEdition()), escape(m.getCategory()), m.getPublicationYear(),
                m.getTimestamp(), escape(m.getDescription()));
    }

    public static String loanToJson(Loan loan) {
        if (loan == null) return "null";
        return String.format(Locale.US,
                "{\"loanId\":\"%s\",\"userId\":\"%s\",\"resourceId\":\"%s\",\"borrowDate\":\"%s\",\"dueDate\":\"%s\",\"returnDate\":%s,\"fine\":%.2f,\"isReturned\":%b}",
                escape(loan.getLoanId()),
                escape(loan.getUserId()),
                escape(loan.getResourceId()),
                loan.getBorrowDate() != null ? loan.getBorrowDate().toString() : "",
                loan.getDueDate() != null ? loan.getDueDate().toString() : "",
                loan.getReturnDate() != null ? "\"" + loan.getReturnDate().toString() + "\"" : "null",
                loan.getFine(),
                loan.isReturned());
    }

    public static String listToJson(List<?> list) {
        if (list == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item instanceof User) {
                sb.append(userToJson((User) item));
            } else if (item instanceof LibraryResource) {
                sb.append(resourceToJson((LibraryResource) item));
            } else if (item instanceof Loan) {
                sb.append(loanToJson((Loan) item));
            } else if (item instanceof memento.ResourceMemento) {
                sb.append(mementoToJson((memento.ResourceMemento) item));
            } else if (item instanceof String) {
                sb.append("\"").append(escape((String) item)).append("\"");
            } else if (item instanceof Number || item instanceof Boolean) {
                sb.append(item);
            } else if (item != null) {
                sb.append(item.toString());
            } else {
                sb.append("null");
            }
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String success(String message, String dataJson) {
        if (dataJson == null || dataJson.trim().isEmpty()) {
            dataJson = "null";
        }
        return String.format("{\"success\":true,\"message\":\"%s\",\"data\":%s}", escape(message), dataJson);
    }

    public static String error(String message, String errorCode, int statusCode) {
        return String.format("{\"success\":false,\"error\":\"%s\",\"errorCode\":\"%s\",\"status\":%d}",
                escape(message), escape(errorCode != null ? errorCode : "ERROR"), statusCode);
    }

    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;

        Pattern stringField = Pattern.compile("\"([a-zA-Z0-9_]+)\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = stringField.matcher(json);
        while (m.find()) {
            map.put(m.group(1), m.group(2));
        }

        Pattern numberOrBoolField = Pattern.compile("\"([a-zA-Z0-9_]+)\"\\s*:\\s*([0-9.]+|true|false)");
        Matcher m2 = numberOrBoolField.matcher(json);
        while (m2.find()) {
            if (!map.containsKey(m2.group(1))) {
                map.put(m2.group(1), m2.group(2));
            }
        }
        return map;
    }
}
