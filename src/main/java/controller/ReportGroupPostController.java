package controller;

import dao.PostDAO;
import dao.ReportGroupPostDAO;
import dao.GroupDAO; // Thêm import GroupDAO
import model.AcceptGroupReportDTO;
import model.ResGroupReportPostDTO;
import model.ResGroupDTO; // Thêm import ResGroupDTO nếu cần
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(
        name = "ReportGroupPostControllerServlet",
        value = "/reportGroupPost"
)
public class ReportGroupPostController extends HttpServlet {

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private final GroupDAO groupDAO = new GroupDAO(); // Thêm GroupDAO instance, tương tự code tham khảo

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ReportGroupPostDAO reportGroupPostDAO = new ReportGroupPostDAO();
        LOGGER.info("Handling GET request for /reportGroupPost");

        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("users");
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        int managerAccountId = account.getId();

        String groupIdParam = request.getParameter("groupId");
        if (groupIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "groupId is required");
            return;
        }

        int groupId;
        try {
            groupId = Integer.parseInt(groupIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid groupId");
            return;
        }

        // Lấy thông tin nhóm từ cơ sở dữ liệu bằng GroupDAO, tương tự code tham khảo
        ResGroupDTO groupInfo = groupDAO.getGroup(groupId);
        if (groupInfo == null) {
            request.setAttribute("groupInfo", null);
            request.setAttribute("error", "Group information not found for groupId: " + groupId);
        } else {
            request.setAttribute("groupInfo", groupInfo);
        }

        try {
            // Lấy danh sách báo cáo cho group
            List<ResGroupReportPostDTO> reportPostList = reportGroupPostDAO.getAllReportsForGroupManager(groupId, managerAccountId);
            LOGGER.info("Successfully retrieved " + reportPostList.size() + " report posts for group ID: " + groupId);

            // Set attribute cho JSP
            request.setAttribute("reportPostList", reportPostList);
            request.setAttribute("groupId", groupId); // Set groupId trực tiếp, tương tự tham khảo

            // Forward đến JSP
            request.getRequestDispatcher("/WEB-INF/views/reported_group.jsp").forward(request, response);
            LOGGER.info("Forwarded request to /WEB-INF/views/reported_group.jsp");
        } catch (NumberFormatException e) {
            LOGGER.severe("Invalid groupId format: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid group ID");
        } catch (SQLException e) {
            LOGGER.severe("Database error retrieving report posts: " + e.getMessage());
            throw new ServletException("Failed to retrieve report posts", e);
        } catch (Exception e) {
            LOGGER.severe("Unexpected error: " + e.getMessage());
            throw new ServletException("Unexpected error", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Handling POST request for /reportGroupPost");

        ReportGroupPostDAO reportGroupPostDAO = new ReportGroupPostDAO();
        PostDAO postDAO = new PostDAO();
        String action = request.getParameter("action");
        if (action == null) {
            LOGGER.warning("No action specified in POST request");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action parameter is required");
            return;
        }

        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("users");
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        int managerAccountId = account.getId();

        try {
            if ("accept".equalsIgnoreCase(action)) {
                // Xử lý accept report
                int reportId = Integer.parseInt(request.getParameter("reportId"));
                int reporterId = Integer.parseInt(request.getParameter("reporterId"));
                int reportedPostId = Integer.parseInt(request.getParameter("reportedPostId"));
                String suspensionMessage = request.getParameter("suspensionMessage");

                // Lấy reportedAccountId từ post
                int reportedAccountId = postDAO.getAccountIdByPostId(reportedPostId);
                if (reportedAccountId == 0) {
                    LOGGER.warning("No account found for reported post ID: " + reportedPostId);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid reported post ID");
                    return;
                }

                // Tạo DTO
                AcceptGroupReportDTO acceptReportDTO = new AcceptGroupReportDTO();
                acceptReportDTO.setReportId(reportId);
                acceptReportDTO.setReportAccountId(reporterId);
                acceptReportDTO.setReportedAccountId(reportedAccountId);
                acceptReportDTO.setReportedPostId(reportedPostId);
                acceptReportDTO.setNotificationContent(suspensionMessage);

                // Xử lý accept cho group
                reportGroupPostDAO.acceptReportForGroup(acceptReportDTO, managerAccountId);
                LOGGER.info("Successfully accepted report ID: " + reportId + " for post ID: " + reportedPostId);
            } else if ("dismiss".equalsIgnoreCase(action)) {
                // Xử lý dismiss report
                int reportId = Integer.parseInt(request.getParameter("reportId"));
                reportGroupPostDAO.dismissReportForGroup(reportId, managerAccountId);
                LOGGER.info("Successfully dismissed report ID: " + reportId);
            } else {
                LOGGER.warning("Invalid action specified: " + action);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                return;
            }

            // Redirect lại trang với groupId
            int groupId = Integer.parseInt(request.getParameter("groupId")); // Giả định groupId được gửi trong POST
            response.sendRedirect(request.getContextPath() + "/reportGroupPost?groupId=" + groupId);
            LOGGER.info("Redirected to /reportGroupPost after processing action: " + action);
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