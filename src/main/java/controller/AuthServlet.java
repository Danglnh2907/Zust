package controller;

import dao.AuthDAO;
import jakarta.servlet.http.*;
import model.Account;
import util.service.FileService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@WebServlet(urlPatterns = {"/auth", "/verify", "/auth/google", "/auth/google/callback", "/logout", "/auth/reset-password"})
@MultipartConfig(
		fileSizeThreshold = 1024 * 1024 * 2,
		maxFileSize = 1024 * 1024 * 10,
		maxRequestSize = 1024 * 1024 * 50
)
public class AuthServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthServlet.class);
	private AuthDAO authDAO;
	private FileService fileService;

	@Override
	public void init() throws ServletException {
		super.init();
		this.fileService = new FileService();
		this.authDAO = new AuthDAO(fileService);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getServletPath();
		String queryString = request.getQueryString();

		// Handle reset password page
		if (path.equals("/auth") && queryString != null && queryString.contains("reset-password")) {
			showResetPasswordPage(request, response);
			return;
		}

		switch (path) {
			case "/verify":
				handleEmailVerification(request, response);
				break;
			case "/auth/google":
				handleGoogleAuth(request, response);
				break;
			case "/auth/google/callback":
				handleGoogleCallback(request, response);
				break;
			case "/auth/reset-password":
				showResetPasswordPage(request, response);
				break;
			case "/logout":
				handleLogout(request, response);
				break;
			default:
				showAuthPage(request, response);
				break;
		}
	}

	private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		response.sendRedirect(request.getContextPath() + "/login?message=" + java.net.URLEncoder.encode("You have been logged out.", StandardCharsets.UTF_8));
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		if ("login".equals(action)) {
			handleLogin(request, response);
		} else if ("register".equals(action)) {
			handleRegistration(request, response);
		} else if ("forgot-password".equals(action)) {
			handleForgotPassword(request, response);
		} else if ("reset-password".equals(action)) {
			handleResetPassword(request, response);
		} else {
			response.sendRedirect(request.getContextPath() + "/auth");
		}
	}

	private void showAuthPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getSession().getAttribute("loggedInAccount") != null) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}

		String successMessage = request.getParameter("successMessage");
		if (successMessage != null && !successMessage.trim().isEmpty()) {
			request.setAttribute("successMessage", successMessage);
		}

		// Set default active tab to login if not specified
		if (request.getAttribute("activeTab") == null) {
			request.setAttribute("activeTab", "login");
		}

		// Set Google OAuth URL
		try {
			String googleAuthUrl = authDAO.getGoogleAuthUrl();
			request.setAttribute("googleAuthUrl", googleAuthUrl);
		} catch (Exception e) {
			LOGGER.error("Failed to generate Google OAuth URL: {}", e.getMessage(), e);
		}

		request.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(request, response);
	}

	private void showResetPasswordPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String token = request.getParameter("token");
		if (token == null || token.trim().isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/auth?error=" + 
				java.net.URLEncoder.encode("Invalid reset link. Please request a new password reset.", StandardCharsets.UTF_8));
			return;
		}

		request.setAttribute("token", token);
		request.getRequestDispatcher("/WEB-INF/views/reset_password.jsp").forward(request, response);
	}

	private void handleGoogleAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String authUrl = authDAO.getGoogleAuthUrl();
			response.sendRedirect(authUrl);
		} catch (Exception e) {
			LOGGER.error("Failed to redirect to Google OAuth: {}", e.getMessage(), e);
			response.sendRedirect(request.getContextPath() + "/auth?error=" +
					java.net.URLEncoder.encode("Failed to initialize Google login", StandardCharsets.UTF_8));
		}
	}

	private void handleGoogleCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String code = request.getParameter("code");
		String error = request.getParameter("error");
		String state = request.getParameter("state");

		if (error != null) {
			LOGGER.warn("Google OAuth error: {}", error);
			response.sendRedirect(request.getContextPath() + "/auth?error=" +
					java.net.URLEncoder.encode("Google authentication was cancelled or failed", StandardCharsets.UTF_8));
			return;
		}

		if (code == null || code.trim().isEmpty()) {
			LOGGER.warn("Google OAuth callback received without authorization code");
			response.sendRedirect(request.getContextPath() + "/auth?error=" +
					java.net.URLEncoder.encode("Invalid Google authentication response", StandardCharsets.UTF_8));
			return;
		}

		if (!"zust-auth".equals(state)) {
			LOGGER.warn("Google OAuth state mismatch. Expected: zust-auth, Received: {}", state);
			response.sendRedirect(request.getContextPath() + "/auth?error=" +
					java.net.URLEncoder.encode("Invalid authentication state", StandardCharsets.UTF_8));
			return;
		}

		try {
			Account account = authDAO.registerOrLoginWithGoogle(code);
			if (account != null) {
				request.getSession().setAttribute("loggedInAccount", account);
				request.getSession().setAttribute("users", account);
				LOGGER.info("Google OAuth successful for user: {}", account.getEmail());
				response.sendRedirect(request.getContextPath() + "/");
			} else {
				LOGGER.error("Google OAuth failed - no account returned");
				response.sendRedirect(request.getContextPath() + "/auth?error=" +
						java.net.URLEncoder.encode("Failed to authenticate with Google", StandardCharsets.UTF_8));
			}
		} catch (Exception e) {
			LOGGER.error("Google OAuth error: {}", e.getMessage(), e);
			response.sendRedirect(request.getContextPath() + "/auth?error=" +
					java.net.URLEncoder.encode("Google authentication failed: " + e.getMessage(), StandardCharsets.UTF_8));
		}
	}

	private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String errorMessage;

		if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
			errorMessage = "Username and password cannot be empty.";
		} else {
			try {
				if(authDAO.isAdminLoggedIn(username, password)) {
					request.getSession().setAttribute("isAdminLoggedIn", true);
					request.getRequestDispatcher("/dashboard").forward(request, response);
					return;
				}

				if (authDAO.loginByForm(username, password)) {
					Account loggedInAccount = authDAO.getAccountByUsername(username);
					if (loggedInAccount != null) {
						request.getSession().setAttribute("loggedInAccount", loggedInAccount);
						request.getSession().setAttribute("users", loggedInAccount);
						response.sendRedirect(request.getContextPath() + "/");
						return;
					} else {
						errorMessage = "Login successful but could not retrieve account details.";
						LOGGER.error("Login successful for username {} but getAccountByUsername returned null.", username);
					}
				} else {
					errorMessage = "Invalid username or password, or account not active.";
				}
			} catch (SQLException e) {
				LOGGER.error("Database error during login for user: {}", username, e);
				errorMessage = "Database error: " + e.getMessage();
			} catch (Exception e) {
				LOGGER.error("Unexpected error during login for user: {}", username, e);
				errorMessage = "An unexpected error occurred.";
			}
		}

		request.setAttribute("errorMessage", errorMessage);
		request.setAttribute("username", username);
		request.setAttribute("activeTab", "login");

		// Set Google OAuth URL for the error page
		try {
			String googleAuthUrl = authDAO.getGoogleAuthUrl();
			request.setAttribute("googleAuthUrl", googleAuthUrl);
		} catch (Exception e) {
			LOGGER.error("Failed to generate Google OAuth URL: {}", e.getMessage(), e);
		}

		request.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(request, response);
	}

	private void handleRegistration(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Account account = new Account();
		String errorMessage = null;
		String successMessage;
		String avatarSavedPath;

		try {
			account.setUsername(request.getParameter("username"));
			account.setPassword(request.getParameter("password"));
			account.setFullname(request.getParameter("fullname"));
			account.setEmail(request.getParameter("email"));
			account.setPhone(request.getParameter("phone"));
			String genderStr = request.getParameter("gender");
			account.setGender(genderStr != null && !genderStr.isEmpty() ? Boolean.parseBoolean(genderStr) : null);
			String dobStr = request.getParameter("dob");
			account.setDob(dobStr != null && !dobStr.isEmpty() ? LocalDate.parse(dobStr) : null);
			account.setBio(request.getParameter("bio"));

			Part avatarPart = request.getPart("avatar");
			if (avatarPart != null && avatarPart.getSize() > 0) {
				String submittedFileName = avatarPart.getSubmittedFileName();
				if (submittedFileName != null && !submittedFileName.isEmpty()) {
					String fileExtension = "";
					int dotIndex = submittedFileName.lastIndexOf('.');
					if (dotIndex > 0 && dotIndex < submittedFileName.length() - 1) {
						fileExtension = submittedFileName.substring(dotIndex);
					}
					String uniqueFileName = UUID.randomUUID() + fileExtension;

					try (InputStream fileContent = avatarPart.getInputStream()) {
						avatarSavedPath = fileService.saveFile(uniqueFileName, fileContent);
						account.setAvatar(avatarSavedPath);
						LOGGER.info("Avatar saved to: {}", avatarSavedPath);
					} catch (IOException e) {
						LOGGER.error("Failed to save avatar file: {}", e.getMessage(), e);
						errorMessage = "Failed to upload avatar: " + e.getMessage();
					}
				}
			}

			if (errorMessage != null) {
				request.setAttribute("errorMessage", errorMessage);
				request.setAttribute("account", account);
				request.setAttribute("activeTab", "register");

				// Set Google OAuth URL for the error page
				try {
					String googleAuthUrl = authDAO.getGoogleAuthUrl();
					request.setAttribute("googleAuthUrl", googleAuthUrl);
				} catch (Exception e) {
					LOGGER.error("Failed to generate Google OAuth URL: {}", e.getMessage(), e);
				}

				request.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(request, response);
				return;
			}

			authDAO.registerAccount(account);
			successMessage = "Registration successful! A verification email has been sent to " + account.getEmail() + ". Please check your inbox.";
			response.sendRedirect(request.getContextPath() + "/auth?successMessage=" + java.net.URLEncoder.encode(successMessage, StandardCharsets.UTF_8));
			return;

		} catch (IllegalArgumentException e) {
			errorMessage = e.getMessage();
			LOGGER.warn("Registration failed due to invalid argument: {}", e.getMessage());
		} catch (DateTimeParseException e) {
			errorMessage = "Invalid Date of Birth format. Please use YYYY-MM-DD.";
			LOGGER.warn("Registration failed due to DOB parsing error: {}", e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Error during account registration for user: {}", account.getUsername(), e);
			errorMessage = "Registration failed: " + e.getMessage();
		}

		request.setAttribute("errorMessage", errorMessage);
		request.setAttribute("account", account);
		request.setAttribute("activeTab", "register");

		// Set Google OAuth URL for the error page
		try {
			String googleAuthUrl = authDAO.getGoogleAuthUrl();
			request.setAttribute("googleAuthUrl", googleAuthUrl);
		} catch (Exception e) {
			LOGGER.error("Failed to generate Google OAuth URL: {}", e.getMessage(), e);
		}

		request.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(request, response);
	}

	private void handleEmailVerification(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String tokenContent = request.getParameter("token");
		String statusMessage;
		boolean isSuccess;

		if (tokenContent == null || tokenContent.trim().isEmpty()) {
			statusMessage = "Verification token is missing.";
			isSuccess = false;
		} else {
			try {
				if (authDAO.verifyToken(tokenContent)) {
					statusMessage = "Email verified successfully! Your account is now active. You can now login.";
					isSuccess = true;
				} else {
					statusMessage = "Email verification failed. The token might be invalid or expired.";
					isSuccess = false;
				}
			} catch (SQLException e) {
				LOGGER.error("Database error during token verification: {}", tokenContent, e);
				statusMessage = "An error occurred during verification. Please try again later.";
				isSuccess = false;
			}
		}
		request.setAttribute("statusMessage", statusMessage);
		request.setAttribute("isSuccess", isSuccess);
		request.getRequestDispatcher("/WEB-INF/views/verify_status.jsp").forward(request, response);
	}

	private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = request.getParameter("email");
		String errorMessage = null;
		String successMessage = null;

		if (email == null || email.trim().isEmpty()) {
			errorMessage = "Email address is required.";
		} else {
			try {
				if (authDAO.initiatePasswordReset(email.trim())) {
					successMessage = "If an account with this email exists, you will receive a password reset link within a few minutes. Please check your inbox and spam folder.";
				} else {
					// Don't reveal if email exists or not for security
					successMessage = "If an account with this email exists, you will receive a password reset link within a few minutes. Please check your inbox and spam folder.";
				}
			} catch (Exception e) {
				LOGGER.error("Error during password reset initiation for email: {}", email, e);
				errorMessage = "An error occurred while processing your request. Please try again later.";
			}
		}

		request.setAttribute("errorMessage", errorMessage);
		request.setAttribute("successMessage", successMessage);
		request.setAttribute("email", email);
		request.setAttribute("activeTab", "forgot");

		// Set Google OAuth URL for the page
		try {
			String googleAuthUrl = authDAO.getGoogleAuthUrl();
			request.setAttribute("googleAuthUrl", googleAuthUrl);
		} catch (Exception e) {
			LOGGER.error("Failed to generate Google OAuth URL: {}", e.getMessage(), e);
		}

		request.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(request, response);
	}

	private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String token = request.getParameter("token");
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");
		String errorMessage = null;
		String successMessage = null;

		if (token == null || token.trim().isEmpty()) {
			errorMessage = "Reset token is missing.";
		} else if (newPassword == null || newPassword.trim().isEmpty()) {
			errorMessage = "New password is required.";
		} else if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
			errorMessage = "Password confirmation is required.";
		} else if (!newPassword.equals(confirmPassword)) {
			errorMessage = "Passwords do not match.";
		} else if (newPassword.length() < 6) {
			errorMessage = "Password must be at least 6 characters long.";
		} else {
			try {
				if (authDAO.resetPassword(token.trim(), newPassword)) {
					successMessage = "Password reset successfully! You can now login with your new password.";
					// Redirect to login page after successful reset
					response.sendRedirect(request.getContextPath() + "/auth?successMessage=" + 
						java.net.URLEncoder.encode(successMessage, StandardCharsets.UTF_8));
					return;
				} else {
					errorMessage = "Password reset failed. The token might be invalid or expired.";
				}
			} catch (SQLException e) {
				LOGGER.error("Database error during password reset: {}", e.getMessage(), e);
				errorMessage = "An error occurred during password reset. Please try again later.";
			}
		}

		request.setAttribute("errorMessage", errorMessage);
		request.setAttribute("token", token);
		request.getRequestDispatcher("/WEB-INF/views/reset_password.jsp").forward(request, response);
	}
}