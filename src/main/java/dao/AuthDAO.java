package dao;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.database.DBContext;
import model.Account;
import model.Token;
import util.service.EmailService;
import util.service.FileService;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

public class AuthDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthDAO.class);
	private final EmailService emailService;
	private final Dotenv dotenv;

	public AuthDAO(FileService fileService) {
		this.emailService = new EmailService(fileService);
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

	private int saveAccount(Account account, Connection conn) throws SQLException {
		String sql = "INSERT INTO account (username, password, fullname, email, phone, gender, dob, avatar, bio, account_status) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'inactive')";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			String hashedPassword = BCrypt.hashpw(account.getPassword(), BCrypt.gensalt());

			stmt.setString(1, account.getUsername());
			stmt.setString(2, hashedPassword);
			stmt.setString(3, account.getFullname());
			stmt.setString(4, account.getEmail());
			stmt.setString(5, account.getPhone());
			stmt.setObject(6, account.getGender());
			stmt.setDate(7, account.getDob() != null ? Date.valueOf(account.getDob()) : null);
			stmt.setString(8, account.getAvatar());
			stmt.setString(9, account.getBio());

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
		String baseUrl = dotenv.get("APP_BASE_URL", "http://localhost:8080");
		if (baseUrl.isBlank()) {
			LOGGER.warn("APP_BASE_URL is not configured, using default: http://localhost:8080");
			baseUrl = "http://localhost:8080";
		}

		// Prepare template variables
		Map<String, Object> variables = new HashMap<>();
		variables.put("fullName", account.getFullname() != null ? account.getFullname() : account.getUsername());
		variables.put("username", account.getUsername());
		variables.put("verificationLink", baseUrl + "/verify?token=" + tokenContent);
		variables.put("supportEmail", dotenv.get("SUPPORT_EMAIL", "uniacad@gmail.com"));
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

		String sql = "SELECT account_id, username, password, fullname, email, phone, gender, dob, avatar, bio, credit, account_status, account_role FROM account WHERE username = ?";
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
					return account;
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Database error while fetching account by username: {}", e.getMessage(), e);
			throw e;
		}
		return null;
	}

	/**
	 * Resend verification email for inactive accounts
	 */
	public boolean resendVerificationEmail(String email) throws Exception {
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("Email cannot be null or empty");
		}

		try (Connection conn = new DBContext().getConnection()) {
			// Get account by email
			String accountSql = "SELECT account_id, username, fullname, email, account_status FROM account WHERE email = ?";
			Account account = null;

			try (PreparedStatement stmt = conn.prepareStatement(accountSql)) {
				stmt.setString(1, email.trim());
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						account = new Account();
						account.setId(rs.getInt("account_id"));
						account.setUsername(rs.getString("username"));
						account.setFullname(rs.getString("fullname"));
						account.setEmail(rs.getString("email"));
						account.setAccountStatus(rs.getString("account_status"));
					}
				}
			}

			if (account == null) {
				LOGGER.warn("No account found with email: {}", email);
				return false;
			}

			if ("active".equalsIgnoreCase(account.getAccountStatus())) {
				LOGGER.info("Account is already active for email: {}", email);
				return false; // Account is already verified
			}

			// Deactivate old tokens
			String deactivateTokensSql = "UPDATE token SET token_status = 1 WHERE account_id = ? AND token_status = 0";
			try (PreparedStatement stmt = conn.prepareStatement(deactivateTokensSql)) {
				stmt.setInt(1, account.getId());
				stmt.executeUpdate();
			}

			// Generate new token
			String tokenContent = generateToken();
			Instant now = Instant.now();
			Instant expiresAt = now.plusSeconds(24 * 60 * 60); // 24 hours

			Token newToken = new Token();
			newToken.setAccount(account);
			newToken.setTokenContent(tokenContent);
			newToken.setExpiresAt(expiresAt);
			newToken.setCreatedAt(now);
			newToken.setTokenStatus(false);

			saveToken(newToken, conn);
			sendVerificationEmail(account, tokenContent);

			LOGGER.info("Verification email resent successfully to: {}", email);
			return true;
		}
	}
}
