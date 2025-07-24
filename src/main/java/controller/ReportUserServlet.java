package controller;

import dao.AccountDAO;
import dao.NotificationDAO;
import dao.PostDAO;
import dao.ReportPostDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.Notification;
import model.ResReportAccountDTO;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(
        name = "ReportUserServlet",
        value = "/reportUser"
)
public class ReportUserServlet extends HttpServlet {

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (session.getAttribute("isAdminLoggedIn") == null || !((boolean) session.getAttribute("isAdminLoggedIn"))) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        AccountDAO accountDAO = new AccountDAO();
        List<ResReportAccountDTO> reportPostList = accountDAO.getAllReports();
        LOGGER.info("Successfully retrieved " + reportPostList.size() + " report posts");

        request.setAttribute("reportPostList", reportPostList);
        request.getRequestDispatcher("/WEB-INF/views/report_user.jsp").forward(request, response);
        LOGGER.info("Forwarded request to /WEB-INF/views/report_post.jsp");

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (session.getAttribute("isAdminLoggedIn") == null || !((boolean) session.getAttribute("isAdminLoggedIn"))) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        AccountDAO accountDAO = new AccountDAO();
        String action = request.getParameter("action");
        if (action == null) {
            LOGGER.warning("No action specified in POST request");
            response.sendRedirect(request.getContextPath() + "/reportPost");
            return;
        }

        try {
            int reportId = Integer.parseInt(request.getParameter("reportId"));
            int reportedId = Integer.parseInt(request.getParameter("reportedId"));
            if ("ban".equalsIgnoreCase(action)) {
                boolean success = accountDAO.acceptReport(reportId) && accountDAO.banAccount(reportedId);

                if(success){
                    LOGGER.info("Successfully accepted report ID: " + reportId + "ban user ID: " + reportedId);
//                    request.setAttribute("msg", "Report accepted successfully");
                } else {
                    LOGGER.warning("Failed to accept report ID: " + reportId);
                    request.setAttribute("msg", "Failed to accept report ID: " + reportId);
                }

            } else if ("warn".equalsIgnoreCase(action)) {

                String message = request.getParameter("message");
                Notification notification = new Notification();
                notification.setTitle("Warning");
                notification.setContent(message);
                notification.setCreateDate(LocalDateTime.now());
                notification.setStatus("sent");
                notification.setAccountId(reportedId);

                NotificationDAO notificationDAO = new NotificationDAO();
                boolean success = notificationDAO.addNotification(notification) && accountDAO.acceptReport(reportId);
                if(success){
                    LOGGER.info("Successfully accepted report ID: " + reportId + "send notification user ID: " + reportedId);
//                    request.setAttribute("msg", "Report accepted successfully");
                    doGet(request, response);
                } else {
                    LOGGER.warning("Failed to accept report ID: " + reportId);
                    request.setAttribute("msg", "Failed to accept report ID: " + reportId);
                }

            } else if ("dismiss".equalsIgnoreCase(action)) {

                boolean success = accountDAO.dismissReport(reportId);
                if(success){
                    LOGGER.info("Successfully dismiss report ID: " + reportId + "send notification user ID: " + reportedId);
//                    request.setAttribute("msg", "Report dismiss successfully");
                    doGet(request, response);
                } else {
                    LOGGER.warning("Failed to dismiss report ID: " + reportId);
                    request.setAttribute("msg", "Failed to dismiss report ID: " + reportId);
                }
            } else {
                LOGGER.warning("Invalid action specified: " + action);
                response.sendRedirect(request.getContextPath() + "/reportUser");
                return;
            }
        } catch (NumberFormatException e) {
            LOGGER.severe("Invalid number format in request parameters: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format");
        } catch (SQLException e) {
            LOGGER.severe("Database error processing report: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error processing report: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Unexpected error processing report: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error processing report: " + e.getMessage());
        }
        doGet(request, response);
    }
}