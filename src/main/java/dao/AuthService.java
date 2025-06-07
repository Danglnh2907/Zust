package dao;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.database.DBContext;
import model.Account;
import model.Token;
import util.service.EmailService;
import util.service.FileService;
import org.mindrot.jbcrypt.BCrypt; // Import BCrypt

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AuthService extends DBContext {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
	private final EmailService emailService;
	private final Dotenv dotenv;

	public AuthService(FileService fileService) {
		this.emailService = new EmailService(fileService);
		this.dotenv = Dotenv.configure().filename("save.env").ignoreIfMissing().load();
	}

	public void registerAccount(Account account) throws Exception {
		LOGGER.info("AuthService: Starting registration for account username: {}", account.getUsername());
		try (Connection conn = this.getConnection()) {
			LOGGER.info("AuthService: Account details before saving: username={}, email={}",
					account.getUsername(), account.getEmail());

			int accountId = saveAccount(account, conn);
			LOGGER.info("AuthService: Account saved with ID: {}", accountId);

			String tokenContent = generateToken();
			Instant expiresAt = Instant.now().plusSeconds(24 * 60 * 60);
			Token token = new Token();
			token.setTokenContent(tokenContent);
			token.setAccount(account);
			token.setExpiresAt(expiresAt);
			token.setCreatedAt(Instant.now());
			token.setTokenStatus(false);

			saveToken(token, conn);
			LOGGER.info("AuthService: Token saved for account ID: {}", account.getId());

			sendVerificationEmail(account, tokenContent);
			LOGGER.info("AuthService: Registered account with email: {} and sent verification email", account.getEmail());
		} catch (SQLException e) {
			LOGGER.error("AuthService: Failed to register account: {}", e.getMessage(), e);
			throw e;
		}
	}

	private int saveAccount(Account account, Connection conn) throws SQLException {
		String sql = "INSERT INTO account (username, password, fullname, email, phone, gender, dob, avatar, bio, account_status) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'inactive')";
		try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			// Hash the password using BCrypt before saving
			String hashedPassword = BCrypt.hashpw(account.getPassword(), BCrypt.gensalt());

			stmt.setString(1, account.getUsername());
			stmt.setString(2, hashedPassword); // Save the hashed password
			stmt.setString(3, account.getFullname());
			stmt.setString(4, account.getEmail());
			stmt.setString(5, account.getPhone());
			stmt.setObject(6, account.getGender());
			stmt.setDate(7, account.getDob() != null ? java.sql.Date.valueOf(account.getDob()) : null);
			stmt.setString(8, account.getAvatar());
			stmt.setString(9, account.getBio());
			int rowsAffected = stmt.executeUpdate();

			if (rowsAffected == 0) {
				throw new SQLException("Failed to insert account");
			}

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				} else {
					throw new SQLException("Failed to retrieve generated account ID");
				}
			}
		}
	}

	private void saveToken(Token token, Connection conn) throws SQLException {
		String sql = "INSERT INTO token (token_content, account_id, expires_at, created_at, token_status) " +
				"VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, token.getTokenContent());
			stmt.setInt(2, token.getAccount().getId());
			stmt.setTimestamp(3, Timestamp.from(token.getExpiresAt()));
			stmt.setTimestamp(4, Timestamp.from(token.getCreatedAt()));
			stmt.setBoolean(5, token.getTokenStatus());
			int rowsAffected = stmt.executeUpdate();

			if (rowsAffected == 0) {
				throw new SQLException("Failed to insert token");
			}

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					token.setId(rs.getInt(1));
				}
			}
		}
	}

	private String generateToken() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private void sendVerificationEmail(Account account, String tokenContent) throws Exception {
		String url = dotenv.get("APP_BASE_URL");
		if (url == null || url.isBlank()) {
			LOGGER.warn("AuthService: APP_BASE_URL not configured. Defaulting to empty string for verification link.");
			url = "";
		}

		Map<String, Object> variables = new HashMap<>();
		variables.put("name", account.getFullname());
		variables.put("verificationLink", url + "/verify?token=" + tokenContent);

		String recipientEmail = account.getEmail();
		if (recipientEmail == null || recipientEmail.isBlank()) {
			LOGGER.error("AuthService: CRITICAL ERROR - Recipient email for account (username: '{}', ID: {}) is NULL or BLANK. Cannot send verification email.", account.getUsername(), account.getId());
			throw new IllegalArgumentException("Recipient email is invalid or missing.");
		}
		recipientEmail = recipientEmail.trim();

		emailService.sendEmail(
				recipientEmail,
				"Verify Your Email",
				"verify-email",
				variables,
				null
		);
	}

	public boolean verifyToken(String tokenContent) throws SQLException {
		LOGGER.info("AuthService: Verifying token: {}", tokenContent);
		try (Connection conn = this.getConnection()) {
			String sql = "SELECT token_id, account_id, expires_at, token_status FROM token " +
					"WHERE token_content = ? AND token_status = 0 AND expires_at > GETDATE()";
			Token token = null;
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setString(1, tokenContent);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						token = new Token();
						token.setId(rs.getInt("token_id"));
						Account account = new Account();
						account.setId(rs.getInt("account_id"));
						token.setAccount(account);
						token.setExpiresAt(rs.getTimestamp("expires_at").toInstant());
						token.setTokenStatus(rs.getBoolean("token_status"));
					}
				}
			}

			if (token == null) {
				LOGGER.warn("AuthService: Invalid or expired token: {}", tokenContent);
				return false;
			}

			String updateAccountSql = "UPDATE account SET account_status = 'active' WHERE account_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(updateAccountSql)) {
				stmt.setInt(1, token.getAccount().getId());
				int rowsAffected = stmt.executeUpdate();
				if (rowsAffected == 0) {
					LOGGER.warn("AuthService: No account found for token: {}", tokenContent);
					return false;
				}
			}

			String updateTokenSql = "UPDATE token SET token_status = 1, updated_at = GETDATE() WHERE token_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(updateTokenSql)) {
				stmt.setInt(1, token.getId());
				stmt.executeUpdate();
			}

			LOGGER.info("AuthService: Verified token: {} for account_id: {}", tokenContent, token.getAccount().getId());
			return true;
		} catch (SQLException e) {
			LOGGER.error("AuthService: Failed to verify token: {}", e.getMessage(), e);
			throw e;
		}
	}

	public boolean loginByForm(String username, String password) throws SQLException {
		LOGGER.info("AuthService: Attempting login for username: {}", username);
		// SQL query to retrieve hashed password and account status for the given username
		String sql = "SELECT password, account_status FROM account WHERE username = ?";
		try (Connection conn = this.getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, username);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String hashedPassword = rs.getString("password");
					String accountStatus = rs.getString("account_status");

					// Verify password using BCrypt
					if (BCrypt.checkpw(password, hashedPassword)) {
						// Check if account is active
						if ("active".equals(accountStatus)) {
							LOGGER.info("AuthService: Login successful for username: {}", username);
							return true;
						} else {
							LOGGER.warn("AuthService: Login failed for username {}: Account is inactive.", username);
							return false;
						}
					} else {
						LOGGER.warn("AuthService: Login failed for username {}: Invalid password.", username);
						return false;
					}
				} else {
					LOGGER.warn("AuthService: Login failed for username {}: Username not found.", username);
					return false;
				}
			}
		} catch (SQLException e) {
			LOGGER.error("AuthService: Database error during login for username {}: {}", username, e.getMessage(), e);
			throw e;
		}
	}
}
