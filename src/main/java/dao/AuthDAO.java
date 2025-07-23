package dao;

import model.Account;
import model.Token;
import util.service.EmailService;
import util.service.FileService;
import util.database.DBContext;
import io.github.cdimascio.dotenv.Dotenv;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuthDAO extends DBContext{
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthDAO.class);
	private final EmailService emailService;
	private final GoogleOAuthService googleOAuthService;
	private final Dotenv dotenv;

	public AuthDAO(FileService fileService) {
		this.emailService = new EmailService(fileService);
		this.googleOAuthService = new GoogleOAuthService();
		this.dotenv = Dotenv.configure().filename("save.env").ignoreIfMissing().load();
	}

	public void registerAccount(Account account) throws Exception {
		LOGGER.info("Registering account: {}", account.getUsername());

		try (Connection conn = new DBContext().getConnection()) {
			// Check for existing data
			if (isUsernameExists(account.getUsername(), conn)) {
				throw new IllegalArgumentException("Username already exists.");
			}
			if (isEmailExists(account.getEmail(), conn)) {
				throw new IllegalArgumentException("Email already exists.");
			}
			if (isPhoneExists(account.getPhone(), conn)) {
				throw new IllegalArgumentException("Phone number already exists.");
			}

			// Save account
			int accountId = saveAccount(account, conn);
			account.setId(accountId);

			// Generate and save verification token
			String tokenContent = generateToken();
			Instant now = Instant.now();
			Instant expiresAt = now.plusSeconds(24 * 60 * 60); // 24 hours

			Token token = new Token();
			token.setAccount(account);
			token.setTokenContent(tokenContent);
			token.setExpiresAt(expiresAt);
			token.setCreatedAt(now);
			token.setTokenStatus(false);

			saveToken(token, conn);

			// Send verification email with improved error handling
			try {
				sendVerificationEmail(account, tokenContent);
				LOGGER.info("Registration complete and verification email sent to {}", account.getEmail());
			} catch (Exception emailException) {
				LOGGER.error("Failed to send verification email to {}: {}", account.getEmail(), emailException.getMessage());
				// Optionally, you might want to rollback the registration or mark it as pending email
				throw new Exception("Account created but failed to send verification email. Please contact support.", emailException);
			}

		} catch (SQLException e) {
			LOGGER.error("Registration failed: {}", e.getMessage(), e);
			throw e;
		}
	}

	public Account registerOrLoginWithGoogle(String authorizationCode) throws Exception {
		LOGGER.info("Processing Google OAuth login/registration");

		try {
			GoogleOAuthService.GoogleUserInfo googleUserInfo = googleOAuthService.getUserInfo(authorizationCode);

			if (googleUserInfo.getEmail() == null || googleUserInfo.getEmail().trim().isEmpty()) {
				throw new Exception("Google account does not have a valid email address");
			}

			try (Connection conn = new DBContext().getConnection()) {
				// Check if account exists by email
				Account existingAccount = getAccountByEmail(googleUserInfo.getEmail(), conn);

				if (existingAccount != null) {
					// Account exists, update Google info if needed and return
					if (existingAccount.getGoogleId() == null) {
						updateGoogleInfo(existingAccount.getId(), googleUserInfo.getId(), conn);
					}
					LOGGER.info("Google login successful for existing account: {}", existingAccount.getEmail());
					return existingAccount;
				} else {
					// Create new account
					Account newAccount = createAccountFromGoogle(googleUserInfo);
					LOGGER.info("Google registration successful for new account: {}", newAccount.getEmail());
					return newAccount;
				}
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Google OAuth communication error: {}", e.getMessage(), e);
			throw new Exception("Failed to communicate with Google OAuth service", e);
		} catch (SQLException e) {
			LOGGER.error("Database error during Google OAuth: {}", e.getMessage(), e);
			throw new Exception("Database error during Google authentication", e);
		}
	}

	private String generateUniqueUsername(String baseUsername, Connection conn) throws SQLException {
		// Clean the base username
		String cleanUsername = baseUsername.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
		if (cleanUsername.length() < 3) {
			cleanUsername = "user" + cleanUsername;
		}

		String username = cleanUsername;
		int counter = 1;

		while (isUsernameExists(username, conn)) {
			username = cleanUsername + counter;
			counter++;
		}

		return username;
	}

	private String downloadAndSaveGoogleProfilePicture(String pictureUrl) throws IOException {
		try {
			FileService fileService = new FileService();
			URL url = new URL(pictureUrl);
			String fileName = "google_profile_" + UUID.randomUUID().toString() + ".jpg";

			try (InputStream inputStream = url.openStream()) {
				String savedFileName = fileService.saveFile(fileName, inputStream);
				LOGGER.info("Google profile picture saved as: {}", savedFileName);
				return savedFileName;
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to download Google profile picture: {}", e.getMessage());
			return null;
		}
	}

	private void updateGoogleInfo(int accountId, String googleId, Connection conn) throws SQLException {
		String sql = "UPDATE account SET google_id = ? WHERE account_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, googleId);
			stmt.setInt(2, accountId);
			stmt.executeUpdate();
		}
	}

	private Account getAccountByEmail(String email, Connection conn) throws SQLException {
		String sql = "SELECT account_id, username, password, fullname, email, phone, gender, dob, avatar, bio, credit, account_status, account_role, google_id FROM account WHERE email = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email.trim());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					Account account = new Account();
					account.setId(rs.getInt("account_id"));
					account.setUsername(rs.getString("username"));
					account.setPassword(rs.getString("password"));
					account.setFullname(rs.getString("fullname"));
					account.setEmail(rs.getString("email"));
					account.setPhone(rs.getString("phone"));
					account.setGender(rs.getBoolean("gender"));
					java.sql.Date sqlDate = rs.getDate("dob");
					account.setDob(sqlDate != null ? sqlDate.toLocalDate() : null);
					account.setAvatar(rs.getString("avatar"));
					account.setBio(rs.getString("bio"));
					account.setCredit(rs.getInt("credit"));
					account.setAccountStatus(rs.getString("account_status"));
					account.setAccountRole(rs.getString("account_role"));
					account.setGoogleId(rs.getString("google_id"));
					return account;
				}
			}
		}
		return null;
	}

	private int saveAccount(Account account, Connection conn) throws SQLException {
		String sql = "INSERT INTO account (username, password, fullname, email, phone, gender, dob, avatar, cover_image, bio, account_status) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'inactive')";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			String hashedPassword = BCrypt.hashpw(account.getPassword(), BCrypt.gensalt());

			stmt.setString(1, account.getUsername());
			stmt.setString(2, hashedPassword);
			stmt.setString(3, account.getFullname());
			stmt.setString(4, account.getEmail());
			stmt.setString(5, account.getPhone());
			stmt.setObject(6, account.getGender());
			stmt.setDate(7, account.getDob() != null ? Date.valueOf(account.getDob()) : null);
			stmt.setString(8, account.getAvatar() != null ? account.getAvatar() : "user.png");
			stmt.setString(9, account.getCoverImage() != null ? account.getCoverImage() : "cover.jpg");
			stmt.setString(10, account.getBio());

			int rows = stmt.executeUpdate();
			if (rows == 0) throw new SQLException("Failed to insert account");

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				} else {
					throw new SQLException("Failed to retrieve account ID");
				}
			}
		}
	}

	private void saveToken(Token token, Connection conn) throws SQLException {
		String sql = "INSERT INTO token (token_content, account_id, expires_at, created_at, token_status) " +
				"VALUES (?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, token.getTokenContent());
			stmt.setInt(2, token.getAccount().getId());
			stmt.setTimestamp(3, Timestamp.from(token.getExpiresAt()));
			stmt.setTimestamp(4, Timestamp.from(token.getCreatedAt()));
			stmt.setBoolean(5, token.getTokenStatus());

			if (stmt.executeUpdate() == 0) throw new SQLException("Failed to insert token");

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) token.setId(rs.getInt(1));
			}
		}
	}

	private String generateToken() {
		byte[] randomBytes = new byte[32];
		new SecureRandom().nextBytes(randomBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
	}

	private void sendVerificationEmail(Account account, String tokenContent) throws Exception {
		// Validate inputs
		if (account == null) {
			throw new IllegalArgumentException("Account cannot be null.");
		}
		if (account.getEmail() == null || account.getEmail().trim().isEmpty()) {
			throw new IllegalArgumentException("Account email cannot be null or empty.");
		}
		if (tokenContent == null || tokenContent.trim().isEmpty()) {
			throw new IllegalArgumentException("Token content cannot be null or empty.");
		}

		// Get base URL from environment
		String baseUrl = dotenv.get("APP_BASE_URL", "http://localhost:8080/zust");
		if (baseUrl.isBlank()) {
			LOGGER.warn("APP_BASE_URL is not configured, using default: http://localhost:8080/zust");
			baseUrl = "http://localhost:8080/zust";
		}

		// Prepare template variables
		Map<String, Object> variables = new HashMap<>();
		variables.put("fullName", account.getFullname() != null ? account.getFullname() : account.getUsername());
		variables.put("username", account.getUsername());
		variables.put("verificationLink", baseUrl + "/zust/verify?token=" + tokenContent);
		variables.put("supportEmail", dotenv.get("SUPPORT_EMAIL", "zust.developer@gmail.com"));
		variables.put("companyName", "Zust Social Media");
		variables.put("currentYear", String.valueOf(java.time.Year.now().getValue()));

		// Prepare image attachments for email template
		List<String> imageAttachments = Arrays.asList(
				"948015252763872ed01b79cbbbb7c68b.png", // Header background
				"f8d71b6c42f7300871f9e091c6a737e3.jpg", // Email icon
				"4d3b20f647cbdeb288013a15cce39fdf.jpg", // Text/SMS icon
				"1cd2ff272e2531b8041264de38db1b5f.png", // X/Twitter icon
				"51a2644c1491853d60a9688ed8f4fa9e.png", // Instagram icon
				"7575b9251670cd15f3423fd911239179.png"  // Facebook icon
		);

		try {
			emailService.sendEmail(
					account.getEmail().trim(),
					"Verify Your Zust Social Media Account",
					"email-verification", // Template name (will look for email-verification.html)
					variables,
					imageAttachments
			);
			LOGGER.info("Verification email sent successfully to: {}", account.getEmail());
		} catch (Exception e) {
			LOGGER.error("Failed to send verification email to {}: {}", account.getEmail(), e.getMessage(), e);
			throw new Exception("Failed to send verification email: " + e.getMessage(), e);
		}
	}

	public boolean verifyToken(String tokenContent) throws SQLException {
		if (tokenContent == null || tokenContent.trim().isEmpty()) {
			LOGGER.warn("Token content is null or empty");
			return false;
		}

		try (Connection conn = new DBContext().getConnection()) {
			String query = "SELECT token_id, account_id, expires_at FROM token WHERE token_content = ? AND token_status = 0 AND expires_at > CURRENT_TIMESTAMP";

			Token token = null;

			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.setString(1, tokenContent.trim());
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						token = new Token();
						token.setId(rs.getInt("token_id"));
						Account account = new Account();
						account.setId(rs.getInt("account_id"));
						token.setAccount(account);
						token.setExpiresAt(rs.getTimestamp("expires_at").toInstant());
					}
				}
			}

			if (token == null) {
				LOGGER.warn("Invalid or expired token: {}", tokenContent);
				return false;
			}

			// Activate account
			try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET account_status = 'active' WHERE account_id = ?")) {
				stmt.setInt(1, token.getAccount().getId());
				if (stmt.executeUpdate() == 0) {
					LOGGER.error("Failed to activate account with ID: {}", token.getAccount().getId());
					return false;
				}
			}

			// Mark token as used
			try (PreparedStatement stmt = conn.prepareStatement("UPDATE token SET token_status = 1, updated_at = CURRENT_TIMESTAMP WHERE token_id = ?")) {
				stmt.setInt(1, token.getId());
				stmt.executeUpdate();
			}

			LOGGER.info("Token verified successfully for account ID: {}", token.getAccount().getId());
			return true;
		} catch (SQLException e) {
			LOGGER.error("Database error during token verification: {}", e.getMessage(), e);
			throw e;
		}
	}

	public boolean loginByForm(String username, String password) throws SQLException {
		if (username == null || username.trim().isEmpty()) {
			LOGGER.warn("Username is null or empty");
			return false;
		}
		if (password == null || password.isEmpty()) {
			LOGGER.warn("Password is null or empty");
			return false;
		}

		String sql = "SELECT password, account_status FROM account WHERE username = ?";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, username.trim());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String hashed = rs.getString("password");
					String status = rs.getString("account_status");

					if (BCrypt.checkpw(password, hashed)) {
						if ("active".equalsIgnoreCase(status)) {
							LOGGER.info("Login successful for user: {}", username);
							return true;
						} else {
							LOGGER.warn("Account is not active: {} (status: {})", username, status);
						}
					} else {
						LOGGER.warn("Invalid password for user: {}", username);
					}
				} else {
					LOGGER.warn("Username not found: {}", username);
				}
				return false;
			}
		} catch (SQLException e) {
			LOGGER.error("Database error during login: {}", e.getMessage(), e);
			throw e;
		}
	}

	private boolean isUsernameExists(String username, Connection conn) throws SQLException {
		String sql = "SELECT 1 FROM account WHERE username = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, username);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	private boolean isEmailExists(String email, Connection conn) throws SQLException {
		String sql = "SELECT 1 FROM account WHERE email = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	private boolean isPhoneExists(String phone, Connection conn) throws SQLException {
		if (phone == null || phone.isBlank()) return false;
		String sql = "SELECT 1 FROM account WHERE phone = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, phone);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	public Account getAccountByUsername(String username) throws SQLException {
		if (username == null || username.trim().isEmpty()) {
			return null;
		}

		String sql = "SELECT account_id, username, password, fullname, email, phone, gender, dob, avatar, bio, credit, account_status, account_role, google_id FROM account WHERE username = ?";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, username.trim());
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					Account account = new Account();
					account.setId(rs.getInt("account_id"));
					account.setUsername(rs.getString("username"));
					account.setPassword(rs.getString("password"));
					account.setFullname(rs.getString("fullname"));
					account.setEmail(rs.getString("email"));
					account.setPhone(rs.getString("phone"));
					account.setGender(rs.getBoolean("gender"));
					java.sql.Date sqlDate = rs.getDate("dob");
					account.setDob(sqlDate != null ? sqlDate.toLocalDate() : null);
					account.setAvatar(rs.getString("avatar"));
					account.setBio(rs.getString("bio"));
					account.setCredit(rs.getInt("credit"));
					account.setAccountStatus(rs.getString("account_status"));
					account.setAccountRole(rs.getString("account_role"));
					account.setGoogleId(rs.getString("google_id"));
					return account;
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Database error while fetching account by username: {}", e.getMessage(), e);
			throw e;
		}
		return null;
	}

	public String getGoogleAuthUrl(String state) {
		return googleOAuthService.getAuthorizationUrl(state);
	}

	public String getGoogleAuthUrl() {
		return googleOAuthService.getAuthorizationUrl();
	}

	public String getGoogleLoginUrl() {
		return googleOAuthService.getAuthorizationUrl("login");
	}

	public String getGoogleRegisterUrl() {
		return googleOAuthService.getAuthorizationUrl("register");
	}

	private Account createAccountFromGoogle(GoogleOAuthService.GoogleUserInfo googleUserInfo) throws SQLException, IOException {
		Account account = new Account();

		// Generate unique username from email
		String baseUsername = googleUserInfo.getEmail().split("@")[0];
		String username = generateUniqueUsername(baseUsername, getConnection());

		account.setUsername(username);
		account.setEmail(googleUserInfo.getEmail());
		account.setFullname(googleUserInfo.getName() != null ? googleUserInfo.getName() : username);
		account.setGoogleId(googleUserInfo.getId());
		account.setAccountStatus("active"); // Google accounts are pre-verified

		// Set default values for required fields
		account.setPassword(""); // Empty password for Google accounts
		account.setCredit(100);  // Default credit value
		account.setAccountRole("user"); // Default role

		// Download and save profile picture if available
		if (googleUserInfo.getPicture() != null && !googleUserInfo.getPicture().isEmpty()) {
			try {
				String avatarPath = downloadAndSaveGoogleProfilePicture(googleUserInfo.getPicture());
				account.setAvatar(avatarPath);
			} catch (Exception e) {
				LOGGER.warn("Failed to download Google profile picture for {}: {}", googleUserInfo.getEmail(), e.getMessage());
			}
		}

		// Save account
		int accountId = saveGoogleAccount(account, getConnection());
		account.setId(accountId);

		return account;
	}

	private int saveGoogleAccount(Account account, Connection conn) throws SQLException {
		String sql = "INSERT INTO account (username, email, fullname, google_id, avatar,cover_image, account_status, " +
				"password, credit, account_role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, account.getUsername());
			stmt.setString(2, account.getEmail());
			stmt.setString(3, account.getFullname());
			stmt.setString(4, account.getGoogleId());
			stmt.setString(5, account.getAvatar());
			stmt.setString(6, account.getCoverImage() != null ? account.getCoverImage() : "cover.jpg");
			stmt.setString(7, account.getAccountStatus());
			stmt.setString(8, account.getPassword()); // Will be empty string
			stmt.setInt(9, account.getCredit());      // Will be 100
			stmt.setString(10, account.getAccountRole()); // Will be "user"

			int rows = stmt.executeUpdate();
			if (rows == 0) throw new SQLException("Failed to insert Google account");

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				} else {
					throw new SQLException("Failed to retrieve account ID");
				}
			}
		}
	}

	public boolean isAdminLoggedIn(String username, String password) {
		Connection conn;
		try {
			conn = new DBContext().getConnection();
			if (conn == null) {
				LOGGER.warn("Failed to connect to database");
				return false;
			}

			String sql = """
					SELECT password FROM account WHERE username= ? AND account_role = 'admin'""";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String hashedPassword = rs.getString("password");
				return BCrypt.checkpw(password, hashedPassword);
			}
			return false;
		} catch (Exception e) {
			LOGGER.warn("Failed to check if admin logged in");
			return false;
		}
	}

	public void registerAdmin(String username, String password) {
		Connection conn;
		try {
			conn = new DBContext().getConnection();
			if (conn == null) {
				LOGGER.warn("Failed to connect to database!");
				return;
			}

			String sql = """
					INSERT INTO account (username, password, fullname, email, account_status, account_role) \
					VALUES (?, ?, 'Zust admin', 'zust.developer@gmail.com', 'active', 'admin')""";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, username);
			stmt.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected == 0) {
				LOGGER.warn("Failed to register admin");
			} else {
				LOGGER.info("Successfully registered admin");
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to Register admin");
		}
	}

	public static void main(String[] args) {
		AuthDAO dao = new AuthDAO(new FileService());
		dao.registerAdmin("admin", "123456");

	}
}