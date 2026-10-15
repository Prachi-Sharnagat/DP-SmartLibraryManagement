package server;

import bridge.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import database.Database;
import factory.LibraryResource;
import factory.ResourceFactory;
import model.*;
import observer.Member;
import observer.ReservationManager;
import proxy.LibraryService;
import proxy.LibraryServiceProxy;
import service.LibraryServiceImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Embedded Java REST API Server for the Smart Library Management System.
 * Connects the 5 GoF Design Patterns and SQLite persistence to the React Web Frontend.
 */
public class LibraryApiServer {
    private final int port;
    private HttpServer server;
    private final LibraryService libraryService;
    private final Database db;
    private final ReservationManager reservationManager;

    public LibraryApiServer(int port) {
        this.port = port;
        this.db = Database.getInstance();
        this.libraryService = new LibraryServiceImpl();
        this.reservationManager = ReservationManager.getInstance();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/logout", new LogoutHandler());
        server.createContext("/api/users/me", new AuthMeHandler());
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/stats", new StatsHandler());
        server.createContext("/api/patterns", new PatternsOverviewHandler());
        server.createContext("/api/resources", new ResourcesHandler());
        server.createContext("/api/resources/copy", new CopyResourceHandler());
        server.createContext("/api/resources/update", new UpdateResourceHandler());
        server.createContext("/api/memento/undo", new MementoUndoHandler());
        server.createContext("/api/memento/history", new MementoHistoryHandler());
        server.createContext("/api/policies/fine-strategy", new FinePolicyHandler());
        server.createContext("/api/policies/preview-fine", new PreviewFineHandler());
        server.createContext("/api/members", new MembersHandler());
        server.createContext("/api/members/pay-fine", new PayFineHandler());
        server.createContext("/api/circulation/issue", new IssueHandler());
        server.createContext("/api/circulation/return", new ReturnHandler());
        server.createContext("/api/loans", new LoansHandler());
        server.createContext("/api/reservations", new ReservationsHandler());
        server.createContext("/api/notifications", new NotificationsHandler());
        server.createContext("/api/notifications/send", new SendNotificationHandler());
        server.createContext("/api/audit-logs", new AuditLogsHandler());

        server.start();
        System.out.println("================================================================================");
        System.out.printf("🚀 SMART LIBRARY REST API SERVER STARTED ON http://localhost:%d/api%n", port);
        System.out.println("   • Health & Telemetry:  http://localhost:" + port + "/api/health");
        System.out.println("   • Catalog Endpoints:   http://localhost:" + port + "/api/resources");
        System.out.println("   • Members Endpoints:   http://localhost:" + port + "/api/members");
        System.out.println("   • Circulation Engine:  http://localhost:" + port + "/api/circulation/issue");
        System.out.println("================================================================================\n");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("  [LibraryApiServer] Server stopped.");
        }
    }

    // HTTP Helper Utilities
    private static void sendCorsAndHeaders(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-User-Id, Accept");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static String getCallerUserId(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String userId = service.AuthService.validateToken(token);
            if (userId != null) {
                return userId;
            }
        }
        return null;
    }

    // 1. Health Handler
    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            String json = String.format("{\"status\":\"UP\",\"sqliteActive\":%b,\"patterns\":[\"Factory Method\",\"Builder\",\"Prototype\",\"Proxy\",\"Bridge\",\"Chain of Responsibility\",\"Observer\",\"Mediator\",\"Template Method\",\"Strategy\",\"Memento\"],\"patternsCount\":11,\"timestamp\":%d}",
                    db.isSqliteActive(), System.currentTimeMillis());
            sendCorsAndHeaders(exchange, 200, JsonUtils.success("System healthy", json));
        }
    }

    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 405, JsonUtils.error("Method Not Allowed", "METHOD_NOT_ALLOWED", 405));
                return;
            }
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtils.parseSimpleJson(body);
            String emailOrId = data.get("emailOrId");
            String password = data.get("password");

            String token = service.AuthService.authenticate(emailOrId, password);
            if (token != null) {
                User user = db.getUserByEmailOrId(emailOrId);
                String json = String.format("{\"token\":\"%s\",\"user\":%s}", token, JsonUtils.userToJson(user));
                sendCorsAndHeaders(exchange, 200, JsonUtils.success("Login successful", json));
            } else {
                sendCorsAndHeaders(exchange, 401, JsonUtils.error("Invalid email or password", "UNAUTHORIZED", 401));
            }
        }
    }

    private class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            service.AuthService.logout(authHeader);
            sendCorsAndHeaders(exchange, 200, JsonUtils.success("Logged out", "null"));
        }
    }

    private class AuthMeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            String caller = getCallerUserId(exchange);
            if (caller == null) {
                sendCorsAndHeaders(exchange, 401, JsonUtils.error("Not authenticated", "UNAUTHORIZED", 401));
                return;
            }
            User user = db.getUser(caller);
            sendCorsAndHeaders(exchange, 200, JsonUtils.success("Authenticated user", JsonUtils.userToJson(user)));
        }
    }

    // 2. Stats Handler
    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }

            List<LibraryResource> resources = db.getAllResources();
            List<User> users = db.getAllUsers();
            List<Loan> loans = db.getAllLoans();
            List<AuditLogEntry> auditLogs = db.getAuditLogs();

            int totalResources = resources.size();
            long availableCount = resources.stream().filter(LibraryResource::isAvailable).count();
            int totalMembers = users.size();
            long activeLoansCount = loans.stream().filter(l -> !l.isReturned()).count();
            double totalFines = users.stream().mapToDouble(User::getOutstandingFine).sum();

            // Calculate average latency from audit logs
            double avgLatency = auditLogs.stream().mapToLong(AuditLogEntry::getLatencyMs).average().orElse(8.5);

            String json = String.format(Locale.US,
                    "{\"totalResources\":%d,\"availableResources\":%d,\"totalMembers\":%d,\"activeLoans\":%d,\"totalFines\":%.2f,\"totalAuditLogs\":%d,\"averageLatencyMs\":%.1f,\"patternsCount\":11}",
                    totalResources, availableCount, totalMembers, activeLoansCount, totalFines, auditLogs.size(), avgLatency);

            sendCorsAndHeaders(exchange, 200, JsonUtils.success("Dashboard statistics", json));
        }
    }

    // 3. Resources Handler (Factory Pattern Integration)
    private class ResourcesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }

            String caller = getCallerUserId(exchange);
            if (caller == null) {
                sendCorsAndHeaders(exchange, 401, JsonUtils.error("Not authenticated", "UNAUTHORIZED", 401));
                return;
            }

            if ("GET".equals(method)) {
                List<LibraryResource> list = db.getAllResources();
                sendCorsAndHeaders(exchange, 200, JsonUtils.listToJson(list));
            } else if ("POST".equals(method)) {
                // Factory Method resource creation
                String body = readRequestBody(exchange);
                Map<String, String> data = JsonUtils.parseSimpleJson(body);

                String type = data.getOrDefault("type", "BOOK").toUpperCase();
                String id = data.getOrDefault("id", "RES-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                String title = data.getOrDefault("title", "Untitled Resource");

                LibraryResource createdResource;
                try {
                    switch (type) {
                        case "EBOOK":
                            String ebAuthor = data.getOrDefault("author", "Unknown Author");
                            double size = Double.parseDouble(data.getOrDefault("fileSizeMB", "10.0"));
                            String format = data.getOrDefault("downloadFormat", "PDF");
                            createdResource = ResourceFactory.createEBook(id, title, ebAuthor, size, format);
                            break;
                        case "JOURNAL": {
                            String jPublisher = data.getOrDefault("publisher", "Academic Press");
                            int vol = Integer.parseInt(data.getOrDefault("volume", "1"));
                            int issue = Integer.parseInt(data.getOrDefault("issue", "1"));
                            boolean peer = Boolean.parseBoolean(data.getOrDefault("isPeerReviewed", "true"));
                            createdResource = ResourceFactory.createJournal(id, title, jPublisher, vol, issue, peer);
                            break;
                        }
                        case "BOOK":
                        default: {
                            String author = data.getOrDefault("author", "Unknown Author");
                            String isbn = data.getOrDefault("isbn", "ISBN-" + UUID.randomUUID().toString().substring(0, 8));
                            String shelf = data.getOrDefault("shelfLocation", "Main Stacks");
                            String publisher = data.getOrDefault("publisher", "Standard Publishing");
                            String edition = data.getOrDefault("edition", "1st Edition");
                            String category = data.getOrDefault("category", "General");
                            int year = 2024;
                            if (data.containsKey("publicationYear")) {
                                try { year = Integer.parseInt(data.get("publicationYear")); } catch (NumberFormatException ignored) {}
                            }
                            int copies = 1;
                            if (data.containsKey("totalCopies")) {
                                try { copies = Integer.parseInt(data.get("totalCopies")); } catch (NumberFormatException ignored) {}
                            }
                            createdResource = new builder.BookBuilder()
                                    .id(id)
                                    .title(title)
                                    .author(author)
                                    .isbn(isbn)
                                    .shelfLocation(shelf)
                                    .publisher(publisher)
                                    .edition(edition)
                                    .category(category)
                                    .publicationYear(year)
                                    .totalCopies(copies)
                                    .availableCopies(copies)
                                    .build();
                            break;
                        }
                    }

                    libraryService.addResource(caller, createdResource);
                    sendCorsAndHeaders(exchange, 201, JsonUtils.success("Resource created via Builder Pattern", JsonUtils.resourceToJson(createdResource)));
                } catch (Exception e) {
                    sendCorsAndHeaders(exchange, 400, JsonUtils.error("Failed to instantiate resource: " + e.getMessage(), "INVALID_PAYLOAD", 400));
                }
            } else if ("DELETE".equals(method)) {
                String query = exchange.getRequestURI().getQuery();
                String resId = null;
                if (query != null && query.contains("id=")) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("id=")) {
                            resId = param.substring(3);
                            break;
                        }
                    }
                }
                if (resId != null && db.deleteResource(resId)) {
                    sendCorsAndHeaders(exchange, 200, JsonUtils.success("Resource deleted", "null"));
                } else {
                    sendCorsAndHeaders(exchange, 404, JsonUtils.error("Resource not found", "NOT_FOUND", 404));
                }
            }
        }
    }

    // 4. Members Handler
    private class MembersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }

            if ("GET".equals(method)) {
                List<User> users = db.getAllUsers();
                sendCorsAndHeaders(exchange, 200, JsonUtils.listToJson(users));
            } else if ("POST".equals(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = JsonUtils.parseSimpleJson(body);

                String id = data.getOrDefault("userId", "U" + (100 + db.getAllUsers().size() + 1));
                String name = data.getOrDefault("name", "New Member");
                String email = data.getOrDefault("email", "member@library.org");
                String phone = data.getOrDefault("phone", "555-0199");
                String role = data.getOrDefault("role", "MEMBER");
                boolean active = Boolean.parseBoolean(data.getOrDefault("membershipActive", "true"));
                String dept = data.getOrDefault("department", "General");
                String sem = data.getOrDefault("semester", "N/A");

                try {
                    User u = new builder.UserBuilder()
                            .userId(id)
                            .name(name)
                            .email(email)
                            .phone(phone)
                            .role(role)
                            .membershipActive(active)
                            .department(dept)
                            .semester(sem)
                            .build();
                    db.saveUser(u);
                    sendCorsAndHeaders(exchange, 201, JsonUtils.success("Member added via Builder Pattern", JsonUtils.userToJson(u)));
                } catch (Exception e) {
                    sendCorsAndHeaders(exchange, 400, JsonUtils.error(e.getMessage(), "INVALID_MEMBER_DATA", 400));
                }
            }
        }
    }

    // 5. Pay Fine Handler
    private class PayFineHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtils.parseSimpleJson(body);
            String userId = data.get("userId");
            double amount = Double.parseDouble(data.getOrDefault("amount", "0.0"));

            if (userId == null) {
                sendCorsAndHeaders(exchange, 400, JsonUtils.error("Missing userId in payload", "BAD_REQUEST", 400));
                return;
            }

            boolean paid = db.payFine(userId, amount);
            if (paid) {
                User u = db.getUser(userId);
                sendCorsAndHeaders(exchange, 200, JsonUtils.success("Fine cleared successfully", JsonUtils.userToJson(u)));
            } else {
                sendCorsAndHeaders(exchange, 404, JsonUtils.error("Member not found", "NOT_FOUND", 404));
            }
        }
    }

    // 6. Issue Handler (Chain of Responsibility + Proxy)
    private class IssueHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }

            String caller = getCallerUserId(exchange);
            if (caller == null) {
                sendCorsAndHeaders(exchange, 401, JsonUtils.error("Not authenticated", "UNAUTHORIZED", 401));
                return;
            }
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtils.parseSimpleJson(body);

            String targetUserId = data.get("targetUserId");
            if (targetUserId == null) targetUserId = data.get("userId");
            String resourceId = data.get("resourceId");

            if (targetUserId == null || resourceId == null) {
                sendCorsAndHeaders(exchange, 400, JsonUtils.error("Must provide userId and resourceId", "INVALID_REQUEST", 400));
                return;
            }

            BorrowResult result = libraryService.borrowResourceDetailed(caller, targetUserId, resourceId);
            if (result.isSuccess()) {
                sendCorsAndHeaders(exchange, 200, result.toJson());
            } else {
                sendCorsAndHeaders(exchange, 422, result.toJson());
            }
        }
    }

    // 7. Return Handler (Observer + Fine Calculation + Proxy)
    private class ReturnHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }

            String caller = getCallerUserId(exchange);
            if (caller == null) {
                sendCorsAndHeaders(exchange, 401, JsonUtils.error("Not authenticated", "UNAUTHORIZED", 401));
                return;
            }
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtils.parseSimpleJson(body);
            String resourceId = data.get("resourceId");

            if (resourceId == null) {
                sendCorsAndHeaders(exchange, 400, JsonUtils.error("Must provide resourceId", "INVALID_REQUEST", 400));
                return;
            }

            ReturnResult result = libraryService.returnResourceDetailed(caller, resourceId);
            if (result.isSuccess()) {
                sendCorsAndHeaders(exchange, 200, result.toJson());
            } else {
                sendCorsAndHeaders(exchange, 400, result.toJson());
            }
        }
    }

    // 8. Loans Handler
    private class LoansHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            List<Loan> loans = db.getAllLoans();
            sendCorsAndHeaders(exchange, 200, JsonUtils.listToJson(loans));
        }
    }

    // 9. Reservations Handler
    private class ReservationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }

            if ("GET".equals(method)) {
                List<ReservationRecord> records = db.getAllReservations();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < records.size(); i++) {
                    sb.append(records.get(i).toJson());
                    if (i < records.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendCorsAndHeaders(exchange, 200, sb.toString());
            } else if ("POST".equals(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = JsonUtils.parseSimpleJson(body);

                String resourceId = data.get("resourceId");
                String userId = data.get("userId");

                if (resourceId == null || userId == null) {
                    sendCorsAndHeaders(exchange, 400, JsonUtils.error("Missing resourceId or userId", "INVALID_REQUEST", 400));
                    return;
                }

                User user = db.getUser(userId);
                LibraryResource res = db.getResource(resourceId);

                if (user == null || res == null) {
                    sendCorsAndHeaders(exchange, 404, JsonUtils.error("User or Resource not found", "NOT_FOUND", 404));
                    return;
                }

                // Register Observer pattern observer
                NotificationChannel inApp = new InAppChannel();
                Notification notif = new BookAvailableNotification(inApp);
                Member observerMember = new Member(user, notif);
                reservationManager.registerObserver(resourceId, observerMember);

                // Record reservation
                String rId = "RES-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                ReservationRecord record = new ReservationRecord(rId, resourceId, res.getTitle(), userId, user.getName());
                db.addReservation(record);

                // Dispatch Bridge pattern notification confirmation
                notif.send(user, "Reservation confirmed for '" + res.getTitle() + "'.");

                db.addNotification(new NotificationRecord(
                        "NOTIF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        user.getUserId(), user.getName(), "IN_APP",
                        "Reservation Confirmed", "You have joined the waitlist for '" + res.getTitle() + "'.", "DELIVERED"
                ));

                sendCorsAndHeaders(exchange, 201, JsonUtils.success("Reservation confirmed and Observer registered", record.toJson()));
            }
        }
    }

    // 10. Notifications Handler
    private class NotificationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            List<NotificationRecord> records = db.getNotifications();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < records.size(); i++) {
                sb.append(records.get(i).toJson());
                if (i < records.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendCorsAndHeaders(exchange, 200, sb.toString());
        }
    }

    // 11. Send Notification (Bridge Pattern Dispatcher)
    private class SendNotificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtils.parseSimpleJson(body);

            String channelName = data.getOrDefault("channel", "IN_APP").toUpperCase();
            String title = data.getOrDefault("title", "Library Alert");
            String message = data.getOrDefault("message", "General Announcement");
            String userId = data.getOrDefault("userId", "U101");

            User u = db.getUser(userId);
            String recipient = u != null ? u.getName() : "Valued Member";

            NotificationChannel channel;
            switch (channelName) {
                case "EMAIL":
                    channel = new EmailChannel();
                    break;
                case "SMS":
                    channel = new SMSChannel();
                    break;
                case "IN_APP":
                default:
                    channel = new InAppChannel();
                    break;
            }

            Notification notif = new BookAvailableNotification(channel);
            notif.send(u, message);

            NotificationRecord record = new NotificationRecord(
                    "NOTIF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                    userId, recipient, channelName, title, message, "DELIVERED"
            );
            db.addNotification(record);

            sendCorsAndHeaders(exchange, 200, JsonUtils.success("Notification sent via Bridge Pattern (" + channelName + ")", record.toJson()));
        }
    }

    // 12. Audit Logs Handler (Proxy Pattern records)
    private class AuditLogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            List<AuditLogEntry> logs = db.getAuditLogs();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < logs.size(); i++) {
                sb.append(logs.get(i).toJson());
                if (i < logs.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendCorsAndHeaders(exchange, 200, sb.toString());
        }
    }

    // 13. Copy Resource Handler (Prototype Pattern)
    private class CopyResourceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 405, JsonUtils.error("Method Not Allowed", "METHOD_NOT_ALLOWED", 405));
                return;
            }
            String caller = getCallerUserId(exchange);
            if (caller == null) {
                sendCorsAndHeaders(exchange, 401, JsonUtils.error("Not authenticated", "UNAUTHORIZED", 401));
                return;
            }
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtils.parseSimpleJson(body);
            String sourceId = data.get("sourceResourceId");
            String newCopyId = data.get("newCopyId");
            String newShelf = data.get("newShelfLocation");

            if (sourceId == null || newCopyId == null) {
                sendCorsAndHeaders(exchange, 400, JsonUtils.error("Missing sourceResourceId or newCopyId", "BAD_REQUEST", 400));
                return;
            }

            try {
                LibraryResource copy = libraryService.createBookCopy(caller, sourceId, newCopyId, newShelf);
                sendCorsAndHeaders(exchange, 201, JsonUtils.success("Physical copy created via Prototype Pattern", JsonUtils.resourceToJson(copy)));
            } catch (Exception e) {
                sendCorsAndHeaders(exchange, 400, JsonUtils.error(e.getMessage(), "PROTOTYPE_COPY_FAILED", 400));
            }
        }
    }

    // 14. Update Resource Handler (Memento Pattern Snapshot)
    private class UpdateResourceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 405, JsonUtils.error("Method Not Allowed", "METHOD_NOT_ALLOWED", 405));
                return;
            }
            String caller = getCallerUserId(exchange);
            if (caller == null) {
                sendCorsAndHeaders(exchange, 401, JsonUtils.error("Not authenticated", "UNAUTHORIZED", 401));
                return;
            }
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtils.parseSimpleJson(body);
            String resourceId = data.get("resourceId");
            if (resourceId == null) {
                sendCorsAndHeaders(exchange, 400, JsonUtils.error("Missing resourceId", "BAD_REQUEST", 400));
                return;
            }
            String title = data.get("title");
            String author = data.get("author");
            String shelf = data.get("shelfLocation");
            String category = data.get("category");
            String publisher = data.get("publisher");
            String edition = data.get("edition");
            int year = 0;
            if (data.containsKey("publicationYear")) {
                try { year = Integer.parseInt(data.get("publicationYear")); } catch (NumberFormatException ignored) {}
            }

            boolean ok = libraryService.updateResourceMetadata(caller, resourceId, title, author, shelf, category, publisher, edition, year);
            if (ok) {
                LibraryResource updated = db.getResource(resourceId);
                sendCorsAndHeaders(exchange, 200, JsonUtils.success("Resource updated. Snapshot saved to Memento history.", JsonUtils.resourceToJson(updated)));
            } else {
                sendCorsAndHeaders(exchange, 400, JsonUtils.error("Failed to update resource", "UPDATE_FAILED", 400));
            }
        }
    }

    // 15. Memento Undo Handler
    private class MementoUndoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 405, JsonUtils.error("Method Not Allowed", "METHOD_NOT_ALLOWED", 405));
                return;
            }
            String caller = getCallerUserId(exchange);
            if (caller == null) {
                sendCorsAndHeaders(exchange, 401, JsonUtils.error("Not authenticated", "UNAUTHORIZED", 401));
                return;
            }
            try {
                boolean ok = libraryService.undoLastEdit(caller);
                if (ok) {
                    sendCorsAndHeaders(exchange, 200, JsonUtils.success("Compensating rollback successfully applied via Memento pattern", "true"));
                } else {
                    sendCorsAndHeaders(exchange, 400, JsonUtils.error("No undoable actions available in history", "NO_HISTORY", 400));
                }
            } catch (Exception e) {
                sendCorsAndHeaders(exchange, 400, JsonUtils.error(e.getMessage(), "UNDO_FAILED", 400));
            }
        }
    }

    // 16. Memento History Handler
    private class MementoHistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            String caller = getCallerUserId(exchange);
            if (caller == null) {
                sendCorsAndHeaders(exchange, 401, JsonUtils.error("Not authenticated", "UNAUTHORIZED", 401));
                return;
            }
            List<memento.ResourceMemento> history = libraryService.getUndoHistory(caller);
            sendCorsAndHeaders(exchange, 200, JsonUtils.listToJson(history));
        }
    }

    // 17. Fine Policy Handler (Strategy Pattern)
    private class FinePolicyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            Map<String, strategy.FineCalculationStrategy> policies = strategy.FineStrategyFactory.getAllPolicies();
            StringBuilder sb = new StringBuilder("[");
            int idx = 0;
            for (Map.Entry<String, strategy.FineCalculationStrategy> entry : policies.entrySet()) {
                strategy.FineCalculationStrategy s = entry.getValue();
                sb.append(String.format(Locale.US,
                        "{\"roleKey\":\"%s\",\"strategyName\":\"%s\",\"dailyRate\":%.2f,\"gracePeriodDays\":%d,\"maxFineLimit\":%.2f}",
                        entry.getKey(), s.getStrategyName(), s.getDailyRate(), s.getGracePeriodDays(), s.getMaxFineLimit()));
                if (idx < policies.size() - 1) sb.append(",");
                idx++;
            }
            sb.append("]");
            sendCorsAndHeaders(exchange, 200, sb.toString());
        }
    }

    // 18. Preview Fine Handler (Strategy Pattern Sandbox)
    private class PreviewFineHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtils.parseSimpleJson(body);
            String userId = data.getOrDefault("userId", "U101");
            String resourceId = data.getOrDefault("resourceId", "B001");
            int overdueDays = 5;
            if (data.containsKey("overdueDays")) {
                try { overdueDays = Integer.parseInt(data.get("overdueDays")); } catch (NumberFormatException ignored) {}
            }
            strategy.FineCalculationResult res = libraryService.previewFine(userId, resourceId, overdueDays);
            sendCorsAndHeaders(exchange, 200, res.toJson());
        }
    }

    // 19. Patterns Overview Handler (11 GoF Patterns Summary)
    private class PatternsOverviewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsAndHeaders(exchange, 204, "");
                return;
            }
            String patternsJson = "[" +
                    "{\"name\":\"Factory Method\",\"category\":\"Creational\",\"description\":\"Instantiates polymorphic resources (Book, EBook, Journal) through dedicated creators.\",\"active\":true}," +
                    "{\"name\":\"Builder\",\"category\":\"Creational\",\"description\":\"Constructs complex Book and User objects with mandatory validation and optional field configuration.\",\"active\":true}," +
                    "{\"name\":\"Prototype\",\"category\":\"Creational\",\"description\":\"Deep-clones catalog templates and creates independent physical book copies without primary key collisions.\",\"active\":true}," +
                    "{\"name\":\"Proxy\",\"category\":\"Structural\",\"description\":\"Intercepts library service operations to enforce RBAC, persistent audit logging, and latency profiling.\",\"active\":true}," +
                    "{\"name\":\"Bridge\",\"category\":\"Structural\",\"description\":\"Decouples notification message abstractions from transport delivery channels (Email, SMS, In-App).\",\"active\":true}," +
                    "{\"name\":\"Chain of Responsibility\",\"category\":\"Behavioral\",\"description\":\"Sequential 5-handler validation pipeline for loan eligibility (Membership, Fine, Limits, Availability, Reservation).\",\"active\":true}," +
                    "{\"name\":\"Observer\",\"category\":\"Behavioral\",\"description\":\"Subscribes members to book waitlists and broadcasts real-time availability alerts upon return.\",\"active\":true}," +
                    "{\"name\":\"Mediator\",\"category\":\"Behavioral\",\"description\":\"Coordinates library circulation across circulation, reservation, fine calculation, and notification services.\",\"active\":true}," +
                    "{\"name\":\"Template Method\",\"category\":\"Behavioral\",\"description\":\"Enforces standardized transaction execution lifecycle with rollback safety for book checkout and check-in.\",\"active\":true}," +
                    "{\"name\":\"Strategy\",\"category\":\"Behavioral\",\"description\":\"Dynamically calculates overdue fines based on member category policies (Standard, Student, Faculty).\",\"active\":true}," +
                    "{\"name\":\"Memento\",\"category\":\"Behavioral\",\"description\":\"Captures pre-mutation snapshots of library records and restores previous states via safe compensating database updates.\",\"active\":true}" +
                    "]";
            sendCorsAndHeaders(exchange, 200, patternsJson);
        }
    }
}
