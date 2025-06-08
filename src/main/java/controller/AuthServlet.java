package controller;

import dao.AuthDAO;
import model.Account;
import util.service.FileService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@WebServlet(urlPatterns = {"/login", "/register", "/verify"}) // Removed /logout mapping from here
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
		// Initialize FileService. It will now handle path resolution using Dotenv internally.
		this.fileService = new FileService();
		this.authDAO = new AuthDAO(fileService);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getServletPath();
		switch (path) {
			case "/login":
				showLoginPage(request, response);
				break;
			case "/register":
				showRegisterPage(request, response);
				break;
			case "/verify":
				handleEmailVerification(request, response);
				break;
			default:
				response.sendRedirect(request.getContextPath() + "/");
				break;
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getServletPath();
		switch (path) {
			case "/login":
				handleLogin(request, response);
				break;
			case "/register":
				handleRegistration(request, response);
				break;
			default:
				response.sendRedirect(request.getContextPath() + "/");
				break;
		}
	}

	private void showLoginPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getSession().getAttribute("loggedInAccount") != null) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}
		request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
	}

	private void showRegisterPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getSession().getAttribute("loggedInAccount") != null) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}
		request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
	}

	private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String errorMessage = null;

		if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
			errorMessage = "Username and password cannot be empty.";
		} else {
			try {
				if (authDAO.loginByForm(username, password)) {
					Account loggedInAccount = authDAO.getAccountByUsername(username);
					if (loggedInAccount != null) {
						request.getSession().setAttribute("loggedInAccount", loggedInAccount);
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
		request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
	}

	private void handleRegistration(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Account account = new Account();
		String errorMessage = null;
		String successMessage = null;
		String avatarSavedPath = null; // To store the path of the saved avatar

		try {
			// Retrieve form fields
			account.setUsername(request.getParameter("username"));
			account.setPassword(request.getParameter("password"));
			account.setFullname(request.getParameter("fullname"));
			account.setEmail(request.getParameter("email"));
			account.setPhone(request.getParameter("phone"));
			String genderStr = request.getParameter("gender");
			account.setGender(genderStr != null ? Boolean.parseBoolean(genderStr) : null);
			String dobStr = request.getParameter("dob");
			account.setDob(dobStr != null && !dobStr.isEmpty() ? LocalDate.parse(dobStr) : null);
			account.setBio(request.getParameter("bio"));

			// Handle avatar file upload
			Part avatarPart = request.getPart("avatar"); // "avatar" is the name of the file input in JSP
			if (avatarPart != null && avatarPart.getSize() > 0) {
				String submittedFileName = avatarPart.getSubmittedFileName();
				if (submittedFileName != null && !submittedFileName.isEmpty()) {
					String fileExtension = "";
					int dotIndex = submittedFileName.lastIndexOf('.');
					if (dotIndex > 0 && dotIndex < submittedFileName.length() - 1) {
						fileExtension = submittedFileName.substring(dotIndex);
					}
					String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

					try (InputStream fileContent = avatarPart.getInputStream()) {
						avatarSavedPath = fileService.saveFile(uniqueFileName, fileContent);
						account.setAvatar(avatarSavedPath); // Store the file path in the account object
						LOGGER.info("Avatar saved to: {}", avatarSavedPath);
					} catch (IOException e) {
						LOGGER.error("Failed to save avatar file: {}", e.getMessage(), e);
						errorMessage = "Failed to upload avatar: " + e.getMessage();
					}
				}
			}

			// If avatar upload failed, we might not want to proceed with registration
			if (errorMessage != null) {
				request.setAttribute("errorMessage", errorMessage);
				request.setAttribute("account", account);
				request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
				return;
			}

			// Proceed with account registration
			authDAO.registerAccount(account);
			successMessage = "Registration successful! A verification email has been sent to " + account.getEmail() + ". Please check your inbox.";
			response.sendRedirect(request.getContextPath() + "/login?successMessage=" + java.net.URLEncoder.encode(successMessage, "UTF-8"));
			return;

		} catch (IllegalArgumentException e) {
			errorMessage = e.getMessage(); // Username/email/phone already exists or invalid file
			LOGGER.warn("Registration failed due to invalid argument: {}", e.getMessage());
		} catch (DateTimeParseException e) {
			errorMessage = "Invalid Date of Birth format. Please use YYYY-MM-DD.";
			LOGGER.warn("Registration failed due to DOB parsing error: {}", e.getMessage());
		} catch (Exception e) { // Catch generic Exception to handle IOException, SQLException, etc.
			LOGGER.error("Error during account registration for user: {}", account.getUsername(), e);
			errorMessage = "Registration failed: " + e.getMessage();
		}

		// If registration fails (and not redirected), set attributes and forward back to form
		request.setAttribute("errorMessage", errorMessage);
		request.setAttribute("account", account); // Pre-fill form fields
		request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
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
}
