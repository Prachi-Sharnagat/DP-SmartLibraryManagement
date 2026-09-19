package service;

import database.Database;
import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication Service
 * Handles user login, password hashing, and session token generation.
 */
public class AuthService {

    // Token -> UserId mapping
    private static final ConcurrentHashMap<String, String> activeSessions = new ConcurrentHashMap<>();

    /**
     * Authenticates a user by email/userId and password.
     * @return A session token (UUID) if successful, null if failed.
     */
    public static String authenticate(String emailOrId, String rawPassword) {
        Database db = Database.getInstance();
        User user = db.getUserByEmailOrId(emailOrId); 
        
        if (user != null && user.getPasswordHash() != null) {
            String hashed = hashPassword(rawPassword);
            if (hashed.equals(user.getPasswordHash())) {
                String token = UUID.randomUUID().toString();
                activeSessions.put(token, user.getUserId());
                return token;
            }
        }
        return null; // Authentication failed
    }

    public static String validateToken(String token) {
        if (token == null) return null;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return activeSessions.get(token);
    }

    public static void logout(String token) {
        if (token != null) {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            activeSessions.remove(token);
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
