package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A custom Servlet for handling HTTP requests.
 * Mapped to /logout by default.
 */
@WebServlet(
		name = "Logout",
		value = "/logout"
)
public class LogoutServlet extends HttpServlet {

	/**
	 * Handle GET requests.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false); // Get existing session, don't create new
		if (session != null) {
			session.invalidate(); // Invalidate the session
		}
		response.sendRedirect(request.getContextPath() + "/login?message=" + java.net.URLEncoder.encode("You have been logged out.", StandardCharsets.UTF_8));
	}

	/**
	 * Handle POST requests.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
