package util.service;

import dao.databaseDao.AccountDAO;
import dto.response.TokenDto;
import model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AccountVerificationService {

	private static final Logger logger = LoggerFactory.getLogger(AccountVerificationService.class);
	private static final String STATUS_PENDING_VERIFICATION = "inactive";
	private static final String STATUS_ACTIVE = "active";
	private static final String APP_BASE_URL = System.getenv("APP_BASE_URL") != null ? System.getenv("APP_BASE_URL") : "http://localhost:8080/social_media";
	private static final String VERIFICATION_PATH = "/verify-email";

	private final AccountDAO accountDAO;
	private final TokenService tokenService;
	private final MailService mailService;

	public AccountVerificationService(AccountDAO accountDAO, TokenService tokenService, MailService mailService) {
		this.accountDAO = accountDAO;
		this.tokenService = tokenService;
		this.mailService = mailService;
		logger.info("AccountVerificationService initialized with provided dependencies.");
	}

	public AccountVerificationService() {
		this(new AccountDAO(), new TokenService(new FileService()), new MailService(null));
	}

	public boolean requestVerificationEmail(String email) {
		if (email == null || email.isBlank()) {
			logger.warn("Verification email request with null or blank email.");
			return false;
		}

		Optional<Account> accountOpt = accountDAO.findByEmail(email);
		if (accountOpt.isEmpty()) {
			logger.warn("Verification email request for non-existent account: {}", email);
			return false;
		}

		Account account = accountOpt.get();
		if (STATUS_ACTIVE.equalsIgnoreCase(account.getAccountStatus())) {
			logger.info("Account {} is already active.", email);
			tokenService.forceDeleteToken(email);
			return true;
		}
		if (!STATUS_PENDING_VERIFICATION.equalsIgnoreCase(account.getAccountStatus())) {
			logger.warn("Account {} status is '{}', not '{}'.", email, account.getAccountStatus(), STATUS_PENDING_VERIFICATION);
			return false;
		}

		String token = tokenService.generateTokenUUID();
		if (!tokenService.saveToken(email, token, 24, TimeUnit.HOURS)) {
			logger.error("Failed to save verification token for {}.", email);
			return false;
		}

		String verificationLink = generateVerificationLink(email, token);
		String userFullName = account.getAccountFullname() != null && !account.getAccountFullname().isBlank()
				? account.getAccountFullname()
				: account.getUsername();

		try {
			Map<String, Object> variables = new HashMap<>();
			variables.put("name", userFullName);
			variables.put("verificationLink", verificationLink);
			variables.put("baseUrl", APP_BASE_URL);

			Map<String, String> failed = mailService.sendPersonalizedWithAttachments(
					"welcome_test",
					"Verify Your Account - Zust App",
					Map.of(email, variables),
					null, // Không có hình ảnh
					null, // Không có tệp đính kèm
					1
			);

			if (failed.containsKey(email)) {
				logger.error("Failed to send verification email to {}: {}", email, failed.get(email));
				tokenService.forceDeleteToken(email);
				return false;
			}
			tokenService.saveToken(email, token, 1, TimeUnit.DAYS);
			mailService.shutdown();
			logger.info("Verification email sent to {} for user {}.", email, userFullName);
			return true;
		} catch (Exception e) {
			logger.error("Error sending verification email to {}: {}", email, e.getMessage(), e);
			tokenService.forceDeleteToken(email);
			return false;
		}
	}

	private String generateVerificationLink(String email, String token) {
		return String.format("%s%s?email=%s&token=%s", APP_BASE_URL, VERIFICATION_PATH, email, token);
	}

	public String verifyEmailWithToken(String email, String token) {
		if (email == null || email.isBlank() || token == null || token.isBlank()) {
			logger.warn("Verification attempt with empty email or token.");
			return "Email and token cannot be empty.";
		}

		Optional<Account> accountOpt = accountDAO.findByEmail(email);
		if (accountOpt.isEmpty()) {
			logger.warn("Verification attempt for non-existent account: {}", email);
			return "Account not found.";
		}

		Account account = accountOpt.get();
		if (STATUS_ACTIVE.equalsIgnoreCase(account.getAccountStatus())) {
			logger.info("Account {} is already active.", email);
			tokenService.forceDeleteToken(email);
			return "Email already verified.";
		}
		if (!STATUS_PENDING_VERIFICATION.equalsIgnoreCase(account.getAccountStatus())) {
			logger.warn("Account {} status is '{}', not '{}'.", email, account.getAccountStatus(), STATUS_PENDING_VERIFICATION);
			return "Account status: " + account.getAccountStatus() + ".";
		}

		if (!tokenService.doesTokenFileExist(email)) {
			logger.warn("Token file does not exist for email '{}'.", email);
			return "Token file not found.";
		}

		if (!tokenService.retrieveAndValidateToken(email, token).isPresent()) {
			logger.warn("Invalid or expired token for email '{}'.", email);
			return "Invalid or expired token.";
		}

		if (accountDAO.updateUserStatus(email, STATUS_ACTIVE)) {
			tokenService.forceDeleteToken(email);
			logger.info("Account {} verified successfully.", email);
			return "Email verified successfully.";
		}

		logger.error("Failed to update account status for {}.", email);
		return "Verification failed due to database error.";
	}

	public static void main(String[] args) throws InterruptedException {
		AccountVerificationService service = new AccountVerificationService();
		String email = "khai1234sd@gmail.com";
		service.requestVerificationEmail(email);
	}
}
