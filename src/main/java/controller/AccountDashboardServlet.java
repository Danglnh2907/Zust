package controller;    
import dao.AccountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(
        name = "AccountDashboardServlet", 
        value = "/accountDashboard"
    )
    public class AccountDashboardServlet extends HttpServlet {

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

            HttpSession session = request.getSession();
            if (session.getAttribute("isAdminLoggedIn") == null || !((boolean) session.getAttribute("isAdminLoggedIn"))) {
                response.sendRedirect(request.getContextPath() + "/auth");
                return;
            }

            AccountDAO accountDAO = new AccountDAO();
            List<Account> accountList = accountDAO.getActiveAccounts();
            request.setAttribute("accountList", accountList);
            request.getRequestDispatcher("/WEB-INF/views/account_dashboard.jsp").forward(request, response);
        }
    
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            LOGGER.info("Handling POST request for /accountDashboard");

            HttpSession session = request.getSession();
            if (session.getAttribute("isAdminLoggedIn") == null || !((boolean) session.getAttribute("isAdminLoggedIn"))) {
                response.sendRedirect(request.getContextPath() + "/auth");
                return;
            }

            AccountDAO accountDAO = new AccountDAO();
            String action = request.getParameter("action");
            if (action == null || !action.equalsIgnoreCase("ban")) {
                LOGGER.warning("Invalid or missing action parameter: " + action);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                return;
            }

            try {
                int accountId = Integer.parseInt(request.getParameter("id"));
                LOGGER.info("Processing ban request for account ID: " + accountId);

                boolean success = accountDAO.banAccount(accountId);
                if (success) {
                    LOGGER.info("Successfully banned account ID: " + accountId);
                } else {
                    LOGGER.warning("Failed to ban account ID: " + accountId);
                    request.setAttribute("msg", "Failed to ban account. It may not exist.");
                    request.getRequestDispatcher("/WEB-INF/views/account_dashboard.jsp").forward(request, response);
                    return;
                }

                // Redirect to the account dashboard page
                response.sendRedirect(request.getContextPath() + "/accountDashboard");
                LOGGER.info("Redirected to /accountDashboard after processing ban action");
            } catch (NumberFormatException e) {
                LOGGER.severe("Invalid account ID format: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid account ID format");
            } catch (Exception e) {
                LOGGER.severe("Unexpected error processing ban request: " + e.getMessage());
                throw new ServletException("Failed to process ban request", e);
            }
        }
    }