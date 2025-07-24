package controller;

import dao.PostDAO;
import dao.ReportPostDAO;
import jakarta.servlet.http.HttpSession;
import model.ResReportPostDTO;
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

        HttpSession session = request.getSession();
        if (session.getAttribute("isAdminLoggedIn") == null || !((boolean) session.getAttribute("isAdminLoggedIn"))) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

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

        HttpSession session = request.getSession();
        if (session.getAttribute("isAdminLoggedIn") == null || !((boolean) session.getAttribute("isAdminLoggedIn"))) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        ReportPostDAO reportPostDAO = new ReportPostDAO();
        String action = request.getParameter("action");
        if (action == null) {
            LOGGER.warning("No action specified in POST request");
            response.sendRedirect(request.getContextPath() + "/reportPost");
            return;
        }

        try {
            int reportId = Integer.parseInt(request.getParameter("reportId"));
            if ("accept".equalsIgnoreCase(action)) {
                // Handle accept report form


                // Process the accept report
                boolean success = reportPostDAO.acceptReport(reportId);

                if(success){
                    LOGGER.info("Successfully accepted report ID: " + reportId);
//                    request.setAttribute("msg", "Report accepted successfully");
                } else {
                    LOGGER.warning("Failed to accept report ID: " + reportId);
                    request.setAttribute("msg", "Failed to accept report ID: " + reportId);
                }
            } else if ("dismiss".equalsIgnoreCase(action)) {
                // Handle dismiss report form
                boolean success = reportPostDAO.dismissReport(reportId);
                if (success) {
                    LOGGER.info("Successfully dismissed report ID: " + reportId);
//                    request.setAttribute("msg", "Report dismissed successfully");
                } else {
                    LOGGER.warning("Failed to dismiss report ID: " + reportId);
                    request.setAttribute("msg", "Failed to dismiss report ID: " + reportId);
                }
                LOGGER.info("Successfully dismissed report ID: " + reportId);
            } else {
                LOGGER.warning("Invalid action specified: " + action);
                response.sendRedirect(request.getContextPath() + "/reportPost");
                return;
            }
        } catch (NumberFormatException e) {
            LOGGER.severe("Invalid number format in request parameters: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format");
        }catch (Exception e) {
            LOGGER.severe("Unexpected error processing report: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error processing report: " + e.getMessage());
        }
        doGet(request, response);
    }
}