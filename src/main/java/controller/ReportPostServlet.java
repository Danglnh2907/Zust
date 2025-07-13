package controller;

import dao.PostDAO;
import dao.ReportPostDAO;
import dto.AcceptReportDTO;
import dto.ResReportPostDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(
        name = "ReportPostServlet",
        value = "/reportPost"
)
public class ReportPostServlet extends HttpServlet {

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ReportPostDAO reportPostDAO = new ReportPostDAO();
        LOGGER.info("Handling GET request for /report-posts");

        try {
            // Retrieve all report posts
            List<ResReportPostDTO> reportPostList = reportPostDAO.getAll();
            LOGGER.info("Successfully retrieved " + reportPostList.size() + " report posts");

            // Set the report post list as a request attribute
            request.setAttribute("reportPostList", reportPostList);

            // Forward to the JSP page
            request.getRequestDispatcher("/WEB-INF/views/report_post.jsp").forward(request, response);
            LOGGER.info("Forwarded request to /WEB-INF/views/report_post.jsp");
        } catch (Exception e) {
            LOGGER.severe("Error retrieving report posts: " + e.getMessage());
            throw new ServletException("Failed to retrieve report posts", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Handling POST request for /report-posts");

        ReportPostDAO reportPostDAO = new ReportPostDAO();
        PostDAO postDAO = new PostDAO();
        String action = request.getParameter("action");
        if (action == null) {
            LOGGER.warning("No action specified in POST request");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action parameter is required");
            return;
        }

        try {
            if ("accept".equalsIgnoreCase(action)) {
                // Handle accept report form
                int reportId = Integer.parseInt(request.getParameter("reportId"));
                int reporterId = Integer.parseInt(request.getParameter("reporterId"));
                int reportedPostId = Integer.parseInt(request.getParameter("reportedPostId"));
                String suspensionMessage = request.getParameter("suspensionMessage");

                // Retrieve reported account ID by looking up the post's account_id
                int reportedAccountId = postDAO.getAccountIdByPostId(reportedPostId);
                if (reportedAccountId == 0) {
                    LOGGER.warning("No account found for reported post ID: " + reportedPostId);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid reported post ID");
                    return;
                }

                // Create AcceptReportDTO
                AcceptReportDTO acceptReportDTO = new AcceptReportDTO();
                acceptReportDTO.setReportId(reportId);
                acceptReportDTO.setReportAccountId(reporterId);
                acceptReportDTO.setReportedAccountId(reportedAccountId);
                acceptReportDTO.setReportedId(reportedPostId);
                acceptReportDTO.setNotificationContent(suspensionMessage);

                // Process the accept report
                reportPostDAO.acceptReport(acceptReportDTO);
                LOGGER.info("Successfully accepted report ID: " + reportId + " for post ID: " + reportedPostId);
            } else if ("dismiss".equalsIgnoreCase(action)) {
                // Handle dismiss report form
                int reportId = Integer.parseInt(request.getParameter("reportId"));
                reportPostDAO.dismissReport(reportId);
                LOGGER.info("Successfully dismissed report ID: " + reportId);
            } else {
                LOGGER.warning("Invalid action specified: " + action);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                return;
            }

            // Redirect to the report posts page
            response.sendRedirect(request.getContextPath() + "/reportPost");
            LOGGER.info("Redirected to /report-posts after processing action: " + action);
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
    }
}