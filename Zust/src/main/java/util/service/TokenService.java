package util.service;

import dto.response.TokenDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenService {

	private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
	private static final String TOKEN_FILE_PREFIX = "token_";
	private static final String TOKEN_FILE_SUFFIX = ".dat";
	private static final FileService.FileType TOKEN_STORAGE_TYPE = FileService.FileType.OTHER; // Hoặc một FileType riêng cho token nếu muốn
	private static final Pattern TOKEN_CONTENT_PATTERN = Pattern.compile(
			"^Token: (\\S+)\\s*\\nEmail: (.*)\\s*\\nCreatedAt: (\\d+)\\s*\\nExpiredAt: (\\d+)$",
			Pattern.MULTILINE
	);

	private final FileService fileService;

	public TokenService(FileService fileService) {
		if (fileService == null) {
			logger.error("FileService cannot be null. TokenService will not function correctly.");
			throw new IllegalArgumentException("FileService cannot be null.");
		}
		this.fileService = fileService;
		logger.info("TokenService initialized.");
	}

	public String generateTokenUUID() {
		return UUID.randomUUID().toString();
	}

	public String getTokenStorageFileName(String email) {
		if (email == null || email.isBlank()) {
			logger.warn("Attempted to generate token file name for null or blank email.");
			// Quyết định trả về gì ở đây: ném exception hay trả về một tên không hợp lệ để các bước sau thất bại một cách an toàn
			throw new IllegalArgumentException("Email cannot be null or blank when generating token file name.");
		}
		String normalizedEmail = email.toLowerCase().trim();
		// Sử dụng Base64 URL an toàn để tránh các ký tự đặc biệt trong tên file
		String encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(normalizedEmail.getBytes(StandardCharsets.UTF_8));
		return TOKEN_FILE_PREFIX + encodedEmail + TOKEN_FILE_SUFFIX;
	}

	/**
	 * Saves or overwrites a token for the given email.
	 *
	 * @param email    The email associated with the token.
	 * @param token    The token string.
	 * @param duration The duration for which the token is valid.
	 * @param unit     The time unit for the duration.
	 * @return true if the token was saved successfully, false otherwise.
	 */
	public boolean saveToken(String email, String token, long duration, TimeUnit unit) {
		if (email == null || email.isBlank()) {
			logger.error("Failed to save token: email is null or blank.");
			return false;
		}
		if (token == null || token.isBlank()) {
			logger.error("Failed to save token for email {}: token is null or blank.", email);
			return false;
		}
		if (duration <= 0) {
			logger.error("Failed to save token for email {}: duration must be positive.", email);
			return false;
		}
		if (unit == null) {
			logger.error("Failed to save token for email {}: time unit is null.", email);
			return false;
		}

		String fileName;
		try {
			fileName = getTokenStorageFileName(email);
		} catch (IllegalArgumentException e) {
			logger.error("Failed to generate token file name for email {}: {}", email, e.getMessage());
			return false;
		}

		long currentTime = System.currentTimeMillis();
		long expiryTime = currentTime + unit.toMillis(duration);

		String content = String.format("Token: %s\nEmail: %s\nCreatedAt: %d\nExpiredAt: %d",
				token, email, currentTime, expiryTime);
		byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

		logger.debug("Attempting to save token for email {} to file {}. Overwrite: true.", email, fileName);
		boolean saved = fileService.saveFile(fileName, contentBytes, TOKEN_STORAGE_TYPE, true); // Always overwrite

		if (saved) {
			logger.info("Token for email {} saved successfully to file {}. Expires at: {}", email, fileName, new Date(expiryTime));
		} else {
			logger.error("Failed to save token for email {} to file {}.", email, fileName);
		}
		return saved;
	}

	/**
	 * Retrieves a token DTO if it exists and is valid (not expired, matches expected token).
	 *
	 * @param email         The email associated with the token.
	 * @param expectedToken The token string to validate against the stored token.
	 * @return Optional containing TokenDto if valid, otherwise empty.
	 */
	public Optional<TokenDto> retrieveAndValidateToken(String email, String expectedToken) {
		if (email == null || email.isBlank()) {
			logger.warn("Token retrieval and validation failed: email is null or blank.");
			return Optional.empty();
		}
		if (expectedToken == null || expectedToken.isBlank()) {
			logger.warn("Token retrieval and validation failed for email {}: expected token is null or blank.", email);
			return Optional.empty();
		}

		String fileName;
		try {
			fileName = getTokenStorageFileName(email);
		} catch (IllegalArgumentException e) {
			logger.warn("Failed to generate token file name for validation for email {}: {}", email, e.getMessage());
			return Optional.empty();
		}

		logger.debug("Attempting to retrieve and validate token for email {} from file {}.", email, fileName);

		Optional<byte[]> fileBytes = fileService.getFileAsBytes(fileName, TOKEN_STORAGE_TYPE);

		if (fileBytes.isEmpty()) {
			logger.info("Token file {} not found for email {}. Validation failed.", fileName, email);
			return Optional.empty();
		}

		try {
			String content = new String(fileBytes.get(), StandardCharsets.UTF_8);
			Matcher matcher = TOKEN_CONTENT_PATTERN.matcher(content);

			if (!matcher.matches()) {
				logger.warn("Invalid token file format in {} for email {}. Deleting potentially corrupt file.", fileName, email);
				deleteTokenFileInternal(fileName, email, "invalid format");
				return Optional.empty();
			}

			String storedToken = matcher.group(1);
			// String storedEmail = matcher.group(2); // Can be used for an additional check if desired
			long createdAt = Long.parseLong(matcher.group(3));
			long expiredAt = Long.parseLong(matcher.group(4));

			TokenDto tokenDto = new TokenDto(storedToken, new Date(createdAt), new Date(expiredAt));

			if (!storedToken.equals(expectedToken)) {
				logger.warn("Token mismatch for email {}. Expected: [{}...], Found: [{}...]. Validation failed.",
						email,
						expectedToken.length() > 10 ? expectedToken.substring(0, 10) : expectedToken,
						storedToken.length() > 10 ? storedToken.substring(0, 10) : storedToken);
				// Không xóa token nếu chỉ là mismatch, có thể người dùng thử token cũ.
				return Optional.empty();
			}

			if (tokenDto.isExpired()) {
				logger.info("Token for email {} from file {} has expired (ExpiredAt: {}). Deleting expired token file.",
						email, fileName, tokenDto.getExpiredAt());
				deleteTokenFileInternal(fileName, email, "expired");
				return Optional.empty();
			}

			logger.info("Token for email {} from file {} validated successfully.", email, fileName);
			return Optional.of(tokenDto);
		} catch (NumberFormatException e) {
			logger.error("Corrupted timestamp in token file {} for email {}. Deleting corrupt file. Error: {}", fileName, email, e.getMessage());
			deleteTokenFileInternal(fileName, email, "corrupted timestamp");
			return Optional.empty();
		} catch (Exception e) { // Catch-all cho các lỗi không mong muốn khác khi xử lý file content
			logger.error("Unexpected error processing token file {} for email {}. Error: {}", fileName, email, e.getMessage(), e);
			// Cân nhắc có nên xóa file trong trường hợp này không. Có thể là lỗi tạm thời.
			// deleteTokenFileInternal(fileName, email, "unexpected processing error");
			return Optional.empty();
		}
	}

	/**
	 * Retrieves the token string from the file if it exists and is not expired.
	 * Does not validate against an expected token.
	 *
	 * @param email The email associated with the token.
	 * @return Optional containing the token string if found and not expired, otherwise empty.
	 */
	public Optional<String> getTokenFromFile(String email) {
		if (email == null || email.isBlank()) {
			logger.warn("Get token from file failed: email is null or blank.");
			return Optional.empty();
		}

		String fileName;
		try {
			fileName = getTokenStorageFileName(email);
		} catch (IllegalArgumentException e) {
			logger.warn("Failed to generate token file name for retrieval for email {}: {}", email, e.getMessage());
			return Optional.empty();
		}

		logger.debug("Attempting to get token from file {} for email {}.", fileName, email);
		Optional<byte[]> fileBytes = fileService.getFileAsBytes(fileName, TOKEN_STORAGE_TYPE);

		if (fileBytes.isEmpty()) {
			logger.info("Token file {} not found for email {}.", fileName, email);
			return Optional.empty();
		}

		try {
			String content = new String(fileBytes.get(), StandardCharsets.UTF_8);
			Matcher matcher = TOKEN_CONTENT_PATTERN.matcher(content);

			if (!matcher.matches()) {
				logger.warn("Invalid token file format in {} for email {}. Deleting potentially corrupt file.", fileName, email);
				deleteTokenFileInternal(fileName, email, "invalid format during get");
				return Optional.empty();
			}

			String storedToken = matcher.group(1);
			long expiredAt = Long.parseLong(matcher.group(4));

			if (System.currentTimeMillis() > expiredAt) {
				logger.info("Token for email {} from file {} has expired. Deleting expired token file.", email, fileName);
				deleteTokenFileInternal(fileName, email, "expired during get");
				return Optional.empty();
			}

			logger.info("Token retrieved from file {} for email {}.", fileName, email);
			return Optional.of(storedToken);
		} catch (NumberFormatException e) {
			logger.error("Corrupted timestamp in token file {} for email {}. Deleting corrupt file. Error: {}", fileName, email, e.getMessage());
			deleteTokenFileInternal(fileName, email, "corrupted timestamp during get");
			return Optional.empty();
		} catch (Exception e) {
			logger.error("Unexpected error processing token file {} for email {} during get. Error: {}", fileName, email, e.getMessage(), e);
			return Optional.empty();
		}
	}

	/**
	 * Consumes (validates and then deletes) a token.
	 *
	 * @param email The email associated with the token.
	 * @param token The token string to validate and consume.
	 * @return true if the token was valid and successfully consumed (deleted), false otherwise.
	 */
	public boolean consumeToken(String email, String token) {
		logger.debug("Attempting to consume token for email {}.", email);
		Optional<TokenDto> tokenDtoOpt = retrieveAndValidateToken(email, token);
		if (tokenDtoOpt.isPresent()) {
			String fileName;
			try {
				fileName = getTokenStorageFileName(email);
			} catch (IllegalArgumentException e) {
				// Điều này không nên xảy ra nếu retrieveAndValidateToken thành công
				logger.error("Internal error: Failed to get filename for already validated token for email {}. Consumption failed.", email);
				return false;
			}
			boolean deleted = deleteTokenFileInternal(fileName, email, "consumed");
			if (deleted) {
				logger.info("Token for email {} consumed successfully.", email);
			} else {
				logger.warn("Token for email {} was validated, but failed to delete the token file {}.", email, fileName);
				// Quyết định xem đây có phải là lỗi nghiêm trọng không. Token vẫn hợp lệ nếu file không bị xóa.
			}
			return deleted; // Hoặc trả về true nếu validation thành công bất kể xóa file có thành công không? Hiện tại là phụ thuộc vào việc xóa.
		}
		logger.warn("Failed to consume token for email {}: token was invalid, not found, or expired.", email);
		return false;
	}

	/**
	 * Forcibly deletes the token file associated with an email, if it exists.
	 *
	 * @param email The email for which to delete the token.
	 * @return true if the token file was deleted or did not exist, false if deletion failed.
	 */
	public boolean forceDeleteToken(String email) {
		if (email == null || email.isBlank()) {
			logger.warn("Force delete token failed: email is null or blank.");
			return false;
		}
		String fileName;
		try {
			fileName = getTokenStorageFileName(email);
		} catch (IllegalArgumentException e) {
			logger.warn("Failed to generate token file name for force deletion for email {}: {}", email, e.getMessage());
			return false;
		}
		logger.debug("Attempting to force delete token file {} for email {}.", fileName, email);
		return deleteTokenFileInternal(fileName, email, "forced deletion");
	}

	private boolean deleteTokenFileInternal(String fileName, String emailForLog, String reason) {
		// fileService.deleteFile nên xử lý việc file không tồn tại và trả về true/false một cách thích hợp.
		boolean deleted = fileService.deleteFile(fileName, TOKEN_STORAGE_TYPE);
		if (deleted) {
			logger.info("Token file {} for email {} deleted successfully. Reason: {}.", fileName, emailForLog, reason);
		} else {
			// Kiểm tra xem file có thực sự không tồn tại không, vì fileService.deleteFile có thể trả về false
			// nếu file không tìm thấy (tùy thuộc vào implementation của FileService.deleteFile)
			// Nếu FileService.deleteFile trả về true nếu file không tồn tại (vì mục tiêu là file không còn), thì không cần check này.
			// Giả sử FileService.deleteFile trả về false nếu file không tồn tại để xóa.
			if (!doesTokenFileExistInternal(fileName)) {
				logger.info("Token file {} for email {} was already not present. Deletion considered successful. Reason: {}.", fileName, emailForLog, reason);
				return true; // File không tồn tại, coi như đã xóa
			}
			logger.warn("Failed to delete token file {} for email {}. Reason: {}.", fileName, emailForLog, reason);
		}
		return deleted;
	}


	/**
	 * Checks if a token file exists for the given email.
	 *
	 * @param email The email to check.
	 * @return true if the token file exists, false otherwise.
	 */
	public boolean doesTokenFileExist(String email) {
		if (email == null || email.isBlank()) {
			logger.warn("Check token file existence failed: email is null or blank.");
			return false;
		}
		String fileName;
		try {
			fileName = getTokenStorageFileName(email);
		} catch (IllegalArgumentException e) {
			logger.warn("Failed to generate token file name for existence check for email {}: {}", email, e.getMessage());
			return false;
		}
		return doesTokenFileExistInternal(fileName);
	}

	private boolean doesTokenFileExistInternal(String fileName) {
		// FileService.findAbsolutePath trả về Optional<String>
		Optional<String> pathOpt = fileService.findAbsolutePath(fileName, TOKEN_STORAGE_TYPE);
		boolean exists = pathOpt.isPresent();
		logger.trace("Token file {} existence check: {}.", fileName, exists);
		return exists;
	}
}
