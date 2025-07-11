package controller;

import dao.AccountDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;

@WebServlet("/change_password")
public class ChangePasswordServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account currentUser = (Account) request.getSession().getAttribute("users");
        if (currentUser == null) {
            response.sendRedirect("login.jsp"); // Redirect to login if not logged in
        } else {
            request.getRequestDispatcher("/WEB-INF/views/change_password.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Input validation
        if (currentPassword == null || newPassword == null || confirmPassword == null ||
                currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            request.setAttribute("error", "All fields are required.");
            request.getRequestDispatcher("change_password.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "New password and confirm password do not match.");
            request.getRequestDispatcher("change_password.jsp").forward(request, response);
            return;
        }

        // Password strength validation
        // At least 8 characters long, includes an uppercase letter, a lowercase letter, and a number.
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        if (!newPassword.matches(passwordRegex)) {
            request.setAttribute("error", "New password does not meet the requirements. It must be at least 8 characters long and include an uppercase letter, a lowercase letter, and a number.");
            request.getRequestDispatcher("change_password.jsp").forward(request, response);
            return;
        }

        Account currentUser = (Account) request.getSession().getAttribute("users");
        int accountId = currentUser.getId();
        AccountDAO accountDAO = new AccountDAO();
        String hashedNewPassword = accountDAO.hashPassword(newPassword);

        if (accountDAO.changePassword(accountId, currentPassword, hashedNewPassword)) {
            response.sendRedirect("post"); // Redirect to homepage
        } else {
            request.setAttribute("error", "Failed to change password. Current password might be incorrect or a database error occurred. Please try again.");
            request.getRequestDispatcher("change_password.jsp").forward(request, response);
        }
    }
}
