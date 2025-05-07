package dk.patientassist.test.auth;

import java.util.Map;

public class AuthManager {
    private static Map<String, String> credentials;
    private static String accessToken;
    private static String refreshToken;

    public static void setCredentials(Map<String, String> creds) {
        credentials = creds;
    }

    public static Map<String, String> getCredentials() {
        return credentials;
    }

    public static void setAccessToken(String token) {
        accessToken = token;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setRefreshToken(String token) {
        refreshToken = token;
    }

    public static String getRefreshToken() {
        return refreshToken;
    }

    public static boolean isAuthenticated() {
        return accessToken != null;
    }

    public static void clear() {
        accessToken = null;
        refreshToken = null;
    }
}
