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

			if (isUsernameExists(account.getUsername(), conn)) {
				throw new IllegalArgumentException("Username already exists.");
			}
			if (isEmailExists(account.getEmail(), conn)) {
				throw new IllegalArgumentException("Email already exists.");
			}
			if (isPhoneExists(account.getPhone(), conn)) {
				throw new IllegalArgumentException("Phone number already exists.");
			}

			int accountId = saveAccount(account, conn);
			account.setId(accountId);

			String tokenContent = generateToken();
			Instant now = Instant.now();
			Instant expiresAt = now.plusSeconds((long) 24 * 60 * 60); // 24 hours

			Token token = new Token();
			token.setAccount(account);
			token.setTokenContent(tokenContent);
			token.setExpiresAt(expiresAt);
			token.setCreatedAt(now);
			token.setTokenStatus(false);

			saveToken(token, conn);
			sendVerificationEmail(account, tokenContent);

			LOGGER.info("Registration complete and verification email sent to {}", account.getEmail());
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
		String baseUrl = dotenv.get("APP_BASE_URL", "");
		if (baseUrl.isBlank()) {
			LOGGER.warn("APP_BASE_URL is not configured properly.");
		}

		Map<String, Object> variables = new HashMap<>();
		variables.put("name", account.getFullname());
		variables.put("verificationLink", baseUrl + "/verify?token=" + tokenContent);

		String recipient = account.getEmail();
		if (recipient == null || recipient.isBlank()) {
			throw new IllegalArgumentException("Email cannot be null or blank.");
		}

		emailService.sendEmail(
				recipient.trim(),
				"Verify Your Email",
				"verify-email",
				variables,
				null
		);
	}

	public boolean verifyToken(String tokenContent) throws SQLException {
		try (Connection conn = new DBContext().getConnection()) {
			String query = "SELECT token_id, account_id, expires_at FROM token WHERE token_content = ? AND token_status = 0 AND expires_at > CURRENT_TIMESTAMP";

			Token token = null;

			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.setString(1, tokenContent);
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
				LOGGER.warn("Invalid or expired token");
				return false;
			}

			try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET account_status = 'active' WHERE account_id = ?")) {
				stmt.setInt(1, token.getAccount().getId());
				if (stmt.executeUpdate() == 0) return false;
			}

			try (PreparedStatement stmt = conn.prepareStatement("UPDATE token SET token_status = 1, updated_at = CURRENT_TIMESTAMP WHERE token_id = ?")) {
				stmt.setInt(1, token.getId());
				stmt.executeUpdate();
			}

			LOGGER.info("Token verified successfully for account ID: {}", token.getAccount().getId());
			return true;
		}
	}

	public boolean loginByForm(String username, String password) throws SQLException {
		String sql = "SELECT password, account_status FROM account WHERE username = ?";
		try (Connection conn = new DBContext().getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, username);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String hashed = rs.getString("password");
					String status = rs.getString("account_status");

					if (BCrypt.checkpw(password, hashed)) {
						if ("active".equalsIgnoreCase(status)) {
							LOGGER.info("Login successful for user: {}", username);
							return true;
						} else {
							LOGGER.warn("Account is not active: {}", username);
						}
					} else {
						LOGGER.warn("Invalid password for user: {}", username);
					}
				} else {
					LOGGER.warn("Username not found: {}", username);
				}
				return false;
			}
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
}
