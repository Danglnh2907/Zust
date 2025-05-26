package controller.servlet.auth;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.service.AccountVerificationService; // Import service của bạn

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "VerificationServlet", urlPatterns = {"/verify-email"})
public class VerificationServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(VerificationServlet.class);
	private AccountVerificationService verificationService;
	private Gson gson;

	@Override
	public void init() throws ServletException {
		super.init();
		this.verificationService = new AccountVerificationService();
		this.gson = new Gson(); // Khởi tạo Gson
		logger.info("VerificationServlet initialized with AccountVerificationService and Gson.");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json"); // Đặt content type là JSON
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");

		String email = request.getParameter("email");
		String token = request.getParameter("token");

		logger.info("Received verification request for email: {} with token: {}",
				(email != null ? email : "null"),
				(token != null ? token.substring(0, Math.min(token.length(), 8)) + "..." : "null"));

		Map<String, Object> jsonResponse = new HashMap<>();
		String status; // "success", "error", "info"
		String message;
		int httpStatusCode = HttpServletResponse.SC_OK; // Mặc định là OK

		if (email == null || email.isBlank() || token == null || token.isBlank()) {
			message = "Invalid verification link. Email or token is missing.";
			status = "error";
			httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;
			logger.warn("Invalid verification link: Email or token is missing.");
		} else {
			try {
				String verificationResult = verificationService.verifyEmailWithToken(email, token);

				switch (verificationResult) {
					case "Email verified successfully.":
						message = "Congratulations! Your email has been successfully verified. You can now log in.";
						status = "success";
						logger.info("Email {} verified successfully.", email);
						break;
					case "Email already verified.":
						message = "This email has already been verified. You can log in.";
						status = "info";
						logger.info("Email {} was already verified.", email);
						break;
					case "Invalid or expired token.":
						message = "The verification link is invalid or has expired. Please request a new verification email.";
						status = "error";
						httpStatusCode = HttpServletResponse.SC_BAD_REQUEST; // Hoặc SC_UNAUTHORIZED tùy ngữ cảnh
						logger.warn("Invalid or expired token for email {}.", email);
						break;
					case "Account not found.":
						message = "No account found associated with this email.";
						status = "error";
						httpStatusCode = HttpServletResponse.SC_NOT_FOUND;
						logger.warn("Verification attempt for non-existent account: {}.", email);
						break;
					case "Token file not found.":
						message = "The verification link is no longer valid. Please request a new verification email.";
						status = "error";
						httpStatusCode = HttpServletResponse.SC_GONE; // Hoặc SC_BAD_REQUEST
						logger.warn("Token file not found for email {}.", email);
						break;
					case "Account status: inactive.": // Giả sử service có thể trả về như vậy
						message = "Account is inactive and awaiting verification. Token seems invalid.";
						status = "error";
						httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;
						logger.warn("Account {} status is inactive, token verification failed.", email);
						break;
					// Thêm các case khác dựa trên thông báo trả về từ service của bạn
					default:
						message = "Verification failed: " + verificationResult + " Please try again or contact support.";
						status = "error";
						httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR; // Hoặc cụ thể hơn nếu biết
						logger.error("Verification failed for email {}: {}", email, verificationResult);
						break;
				}
			} catch (Exception e) {
				logger.error("Error during email verification process for email {}: {}", email, e.getMessage(), e);
				message = "An unexpected error occurred during verification. Please try again later.";
				status = "error";
				httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			}
		}

		jsonResponse.put("status", status);
		jsonResponse.put("message", message);
		if (email != null) { // Có thể thêm email vào response nếu muốn
			jsonResponse.put("email", email);
		}

		response.setStatus(httpStatusCode); // Đặt HTTP status code

		try (PrintWriter out = response.getWriter()) {
			out.print(gson.toJson(jsonResponse));
			out.flush();
		}
	}
}
