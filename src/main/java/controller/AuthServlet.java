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

@WebServlet(urlPatterns = {"/auth", "/verify"})
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
		if ("/verify".equals(path)) {
			handleEmailVerification(request, response);
		} else {
			showAuthPage(request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		if ("login".equals(action)) {
			handleLogin(request, response);
		} else if ("register".equals(action)) {
			handleRegistration(request, response);
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
		request.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(request, response);
	}

	private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String errorMessage = null;

		if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
			errorMessage = "Username and password cannot be empty.";
		} else {
			try {
				if(username.equals("admin") && password.equals("123456")) {
					response.sendRedirect(request.getContextPath() + "/dashboard");
					return;
				}

				if (authDAO.loginByForm(username, password)) {
					Account loggedInAccount = authDAO.getAccountByUsername(username);
					if (loggedInAccount != null) {
						request.getSession().setAttribute("users", loggedInAccount);
						if (loggedInAccount.getAccountRole().equals("admin"))
						{
							request.getSession().setAttribute("isAdmin", true);
							response.sendRedirect(request.getContextPath() + "/admin");
						}
						response.sendRedirect(request.getContextPath() + "/post");
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
		request.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(request, response);
	}

	private void handleRegistration(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Account account = new Account();
		String errorMessage = null;
		String successMessage = null;
		String avatarSavedPath = null;

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
					String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

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
				request.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(request, response);
				return;
			}

			authDAO.registerAccount(account);
			successMessage = "Registration successful! A verification email has been sent to " + account.getEmail() + ". Please check your inbox.";
			response.sendRedirect(request.getContextPath() + "/auth?successMessage=" + java.net.URLEncoder.encode(successMessage, "UTF-8"));
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
}
