package controller;

import dao.ReportCommentDAO;
import model.ResReportCommentDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(
        name = "ReportCommentServlet",
        value = "/reportComment"
)
public class ReportCommentServlet extends HttpServlet {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ReportCommentDAO reportCommentDAO = new ReportCommentDAO();
        List<ResReportCommentDTO> reports = reportCommentDAO.getAll();
        request.setAttribute("reports", reports);
        logger.info("Retrieved " + reports.size() + " comment reports");
        request.getRequestDispatcher("/WEB-INF/views/report_comment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ReportCommentDAO reportCommentDAO = new ReportCommentDAO();
        String action = request.getParameter("action");
        String reportIdParam = request.getParameter("reportId");

        // Validate action
        if (action == null) {
            logger.warning("No action provided for POST request");
            doGet(request, response);
            return;
        }

        // Validate reportId
        int reportId;
        try {
            reportId = Integer.parseInt(reportIdParam);
        } catch (NumberFormatException e) {
            logger.warning("Invalid report ID format: " + reportIdParam);
            request.setAttribute("msg", "Invalid report ID");
            doGet(request, response);
            return;
        }

        logger.info("Processing POST action: " + action + " for report ID: " + reportId);
        switch (action) {
            case "accept":
                // Process accept action
                boolean acceptSuccess = reportCommentDAO.acceptReport(reportId);
                if (acceptSuccess) {
                    logger.info("Successfully accepted report ID: " + reportId);
//                    request.setAttribute("msg", "Report accepted successfully");
                    doGet(request, response);
                } else {
                    logger.warning("Failed to accept report ID: " + reportId);
                    request.setAttribute("msg", "Failed to accept report ID: " + reportId);
                    doGet(request, response);
                }
                break;

            case "dismiss":
                // Process dismiss action
                boolean dismissSuccess = reportCommentDAO.dismissReport(reportId);
                if (dismissSuccess) {
                    logger.info("Successfully dismissed report ID: " + reportId);
//                    request.setAttribute("msg", "Report dismissed successfully");
                    doGet(request, response);
                } else {
                    logger.warning("Failed to dismiss report ID: " + reportId);
                    request.setAttribute("msg", "Failed to dismiss report ID: " + reportId);
                    doGet(request, response);
                }
                break;

            default:
                logger.warning("Invalid action: " + action + " for report ID: " + reportId);
                request.setAttribute("msg", "Invalid action: " + action + " for report ID: " + reportId);
                doGet(request, response);
                break;
        }
    }
}