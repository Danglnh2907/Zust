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
import java.util.regex.Pattern;

@WebServlet(name = "SendEmailServlet", urlPatterns = {"/resend-verification-email", "/request-verification-email"})
public class SendEmailServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(SendEmailServlet.class);
	private AccountVerificationService verificationService;
	private Gson gson;

	// Đơn giản regex cho email, có thể dùng regex phức tạp hơn hoặc thư viện validation
	private static final Pattern EMAIL_PATTERN = Pattern.compile(
			"^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
	);

	@Override
	public void init() throws ServletException {
		super.init();
		this.verificationService = new AccountVerificationService();
		this.gson = new Gson();
		logger.info("SendEmailServlet initialized with AccountVerificationService and Gson.");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");

		String email = request.getParameter("email");

		logger.info("Received request to send verification email to: {}", (email != null ? email : "null"));

		Map<String, Object> jsonResponse = new HashMap<>();
		String status;
		String message;
		int httpStatusCode = HttpServletResponse.SC_OK;

		if (email == null || email.isBlank()) {
			message = "Email address is required.";
			status = "error";
			httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;
			logger.warn("Request to send verification email failed: Email is missing.");
		} else if (!isValidEmail(email)) {
			message = "Invalid email address format.";
			status = "error";
			httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;
			logger.warn("Request to send verification email failed: Invalid email format for '{}'.", email);
		} else {
			try {
				boolean emailSent = verificationService.requestVerificationEmail(email);

				if (emailSent) {
					// Kiểm tra thêm trạng thái tài khoản để cung cấp thông báo chính xác hơn
					// Điều này có thể cần service trả về nhiều thông tin hơn thay vì chỉ boolean
					// Tạm thời giả định: nếu emailSent là true, thì hoặc đã gửi hoặc tài khoản đã active
					String accountStatusInfo = getAccountStatusMessage(email); // Hàm helper (xem bên dưới)

					if (accountStatusInfo.equals("ALREADY_ACTIVE")) {
						message = "This account is already active. You can log in.";
						status = "info"; // Hoặc "success"
						logger.info("Verification email request for {} - account already active.", email);
					} else {
						message = "A verification email has been sent to " + email + ". Please check your inbox (and spam folder).";
						status = "success";
						logger.info("Verification email successfully requested for {}.", email);
					}
				} else {
					// Nếu requestVerificationEmail trả về false, có thể do nhiều lý do
					// Ví dụ: tài khoản không tồn tại, tài khoản không ở trạng thái "inactive", lỗi lưu token, lỗi gửi mail.
					// AccountVerificationService nên log chi tiết lý do thất bại.
					// Ở đây, chúng ta cung cấp một thông báo chung hơn hoặc dựa trên một mã lỗi nếu có.
					message = "Failed to send verification email. The account may not exist, or an internal error occurred. Please try again or contact support if the problem persists.";
					status = "error";
					// Không set httpStatusCode thành lỗi server trừ khi chắc chắn
					// Có thể là lỗi phía client (ví dụ: email không tồn tại)
					httpStatusCode = HttpServletResponse.SC_BAD_REQUEST; // Hoặc SC_INTERNAL_SERVER_ERROR nếu service báo lỗi nội bộ
					logger.warn("Failed to process verification email request for {}. Service returned false.", email);
				}
			} catch (Exception e) {
				logger.error("Error during request for verification email for {}: {}", email, e.getMessage(), e);
				message = "An unexpected error occurred while processing your request. Please try again later.";
				status = "error";
				httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			}
		}

		jsonResponse.put("status", status);
		jsonResponse.put("message", message);
		if (email != null) {
			jsonResponse.put("email", email);
		}

		response.setStatus(httpStatusCode);

		try (PrintWriter out = response.getWriter()) {
			out.print(gson.toJson(jsonResponse));
			out.flush();
		}
	}

	private boolean isValidEmail(String email) {
		if (email == null) {
			return false;
		}
		return EMAIL_PATTERN.matcher(email).matches();
	}

	/**
	 * Hàm helper này là một ví dụ.
	 * Để có thông báo chính xác, AccountVerificationService.requestVerificationEmail
	 * nên trả về một đối tượng kết quả chi tiết hơn là chỉ boolean.
	 * Hoặc bạn có thể gọi một phương thức khác từ AccountDAO/AccountService để lấy trạng thái.
	 */
	private String getAccountStatusMessage(String email) {
		// Đây là phần giả định, bạn cần implement logic thực tế
		// Ví dụ:
		// Optional<Account> accOpt = accountDAO.findByEmail(email);
		// if (accOpt.isPresent() && "active".equalsIgnoreCase(accOpt.get().getAccountStatus())) {
		//     return "ALREADY_ACTIVE";
		// }
		// return "PENDING_OR_SENT";

		// Vì AccountVerificationService.requestVerificationEmail đã có logic kiểm tra active,
		// nếu nó trả về true và tài khoản đã active, nó sẽ log và không gửi lại.
		// Chúng ta có thể giả định nếu service trả về true, mail đã được gửi hoặc không cần gửi vì đã active.
		// Để làm tốt hơn, service cần trả về mã trạng thái cụ thể.
		// Lấy tạm thời từ log của AccountVerificationService (nếu nó log "Account ... is already active.")
		// Tuy nhiên, dựa vào log không phải là cách tốt.
		// Trong AccountVerificationService, nếu account đã active và bạn `return true`,
		// bạn cần một cách để servlet này biết được điều đó.
		// Cách đơn giản là `requestVerificationEmail` trả về một Enum hoặc String code:
		// "SENT", "ALREADY_ACTIVE", "NOT_FOUND", "ERROR_SENDING", "INVALID_STATUS"

		// Giả sử requestVerificationEmail trả về true nếu email được gửi hoặc tài khoản đã active.
		// Nếu muốn phân biệt, bạn cần sửa AccountVerificationService.requestVerificationEmail.
		// Ví dụ, nếu nó trả về true VÀ service log "Account ... is already active." trước khi trả về true,
		// chúng ta có thể cần một cách tốt hơn.

		// Tạm thời, chúng ta không thể chắc chắn 100% từ boolean trả về.
		// Logic an toàn nhất là thông báo email đã được gửi nếu `emailSent` là true.
		// Nếu service được sửa để trả về mã trạng thái, logic ở đây sẽ tốt hơn.
		return "CHECK_SERVICE_LOGIC"; // Placeholder
	}
}
