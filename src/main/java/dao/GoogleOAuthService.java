
package dao;

import io.github.cdimascio.dotenv.Dotenv;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class GoogleOAuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleOAuthService.class);
    private final Dotenv dotenv;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GoogleOAuthService() {
        this.dotenv = Dotenv.configure().filename("save.env").ignoreIfMissing().load();
        this.clientId = dotenv.get("GOOGLE_CLIENT_ID");
        this.clientSecret = dotenv.get("GOOGLE_CLIENT_SECRET");
        this.redirectUri = dotenv.get("GOOGLE_REDIRECT_URI", "http://localhost:8080/zust/auth/google/callback");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String getAuthorizationUrl() {
        return getAuthorizationUrl("zust-auth");
    }

    public String getAuthorizationUrl(String state) {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID is not configured in save.env");
        }

        return "https://accounts.google.com/o/oauth2/v2/auth" + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
               "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
               "&response_type=code" +
               "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8) +
               "&state=" + URLEncoder.encode(state != null ? state : "zust-auth", StandardCharsets.UTF_8) +
               "&access_type=offline" +
               "&prompt=consent";
    }

    public GoogleUserInfo getUserInfo(String authorizationCode) throws IOException, InterruptedException {
        // Step 1: Exchange authorization code for access token
        String accessToken = exchangeCodeForToken(authorizationCode);

        // Step 2: Use access token to get user info
        return fetchUserInfo(accessToken);
    }

    private String exchangeCodeForToken(String authorizationCode) throws IOException, InterruptedException {
        if (clientId == null || clientSecret == null) {
            throw new IllegalStateException("Google OAuth credentials are not configured in save.env");
        }

        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("code", authorizationCode);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", redirectUri);

        String requestBody = params.entrySet()
                .stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .reduce((p1, p2) -> p1 + "&" + p2)
                .orElse("");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            LOGGER.error("Failed to exchange code for token. Status: {}, Body: {}",
                    response.statusCode(), response.body());
            throw new IOException("Failed to exchange authorization code for token: " + response.body());
        }

        JsonNode jsonResponse = objectMapper.readTree(response.body());
        String accessToken = jsonResponse.get("access_token").asText();

        if (accessToken == null || accessToken.isEmpty()) {
            throw new IOException("Access token not found in response");
        }

        return accessToken;
    }

    private GoogleUserInfo fetchUserInfo(String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            LOGGER.error("Failed to fetch user info. Status: {}, Body: {}",
                    response.statusCode(), response.body());
            throw new IOException("Failed to fetch user info: " + response.body());
        }

        JsonNode userInfoJson = objectMapper.readTree(response.body());

        GoogleUserInfo userInfo = new GoogleUserInfo();
        userInfo.setId(userInfoJson.get("id").asText());
        userInfo.setEmail(userInfoJson.get("email").asText());
        userInfo.setName(userInfoJson.get("name").asText());
        userInfo.setGivenName(userInfoJson.has("given_name") ? userInfoJson.get("given_name").asText() : null);
        userInfo.setFamilyName(userInfoJson.has("family_name") ? userInfoJson.get("family_name").asText() : null);
        userInfo.setPicture(userInfoJson.has("picture") ? userInfoJson.get("picture").asText() : null);
        userInfo.setVerifiedEmail(userInfoJson.has("verified_email") && userInfoJson.get("verified_email").asBoolean());

        return userInfo;
    }

    public static class GoogleUserInfo {
        private String id;
        private String email;
        private String name;
        private String givenName;
        private String familyName;
        private String picture;
        private boolean verifiedEmail;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getGivenName() { return givenName; }
        public void setGivenName(String givenName) { this.givenName = givenName; }

        public String getFamilyName() { return familyName; }
        public void setFamilyName(String familyName) { this.familyName = familyName; }

        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }

        public boolean isVerifiedEmail() { return verifiedEmail; }
        public void setVerifiedEmail(boolean verifiedEmail) { this.verifiedEmail = verifiedEmail; }
    }
}