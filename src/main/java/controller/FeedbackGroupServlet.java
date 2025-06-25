package controller;

import dao.FeedbackGroupDAO;
import dto.ResGroupDTO;
import dao.GroupDAO;
import dto.FeedbackGroupDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/viewFeedback")
public class FeedbackGroupServlet extends HttpServlet {
    private final FeedbackGroupDAO feedbackDAO = new FeedbackGroupDAO();
    private final GroupDAO groupDAO = new GroupDAO(); // Khởi tạo GroupDAO
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String groupIdParam = req.getParameter("groupId");
        if (groupIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "groupId is required");
            return;
        }

        int groupId;
        try {
            groupId = Integer.parseInt(groupIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid groupId");
            return;
        }

        ResGroupDTO groupInfo = groupDAO.getActiveGroup(groupId);
        if (groupInfo == null) {
            req.setAttribute("groupInfo", null);
            req.setAttribute("error", "Group information not found for groupId: " + groupId);
        } else {
            req.setAttribute("groupInfo", groupInfo);
        }

        // Giả sử không cần GroupDAO riêng, chỉ lấy feedback
        int managerId = 1; // Hardcode để test
        List<FeedbackGroupDTO> feedbacks = feedbackDAO.getFeedbacksByGroupId(groupId, true); // report_status = 1
        req.setAttribute("feedbacks", feedbacks);
        req.setAttribute("groupId", groupId);
        req.setAttribute("csrfToken", UUID.randomUUID().toString());
        req.getSession().setAttribute("csrfToken", req.getAttribute("csrfToken"));

        req.getRequestDispatcher("/WEB-INF/views/viewFeedback.jsp").forward(req, resp);
    }

//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        int managerId = 1; // Hardcode để test
//
//        String sessionCsrfToken = (String) req.getSession().getAttribute("csrfToken");
//        String requestCsrfToken = req.getParameter("csrfToken");
//        if (sessionCsrfToken == null || !sessionCsrfToken.equals(requestCsrfToken)) {
//            req.setAttribute("error", "Invalid CSRF token");
//            doGet(req, resp);
//            return;
//        }
//
//        String feedbackIdParam = req.getParameter("feedbackId");
//        String action = req.getParameter("action");
//        String groupIdParam = req.getParameter("groupId");
//
//        if (feedbackIdParam == null || action == null || groupIdParam == null) {
//            req.setAttribute("error", "Feedback ID, action, and groupId are required");
//            doGet(req, resp);
//            return;
//        }
//
//        int feedbackId;
//        try {
//            feedbackId = Integer.parseInt(feedbackIdParam);
//        } catch (NumberFormatException e) {
//            req.setAttribute("error", "Invalid feedback ID");
//            doGet(req, resp);
//            return;
//        }
//
//        if (!"approve".equalsIgnoreCase(action) && !"disapprove".equalsIgnoreCase(action)) {
//            req.setAttribute("error", "Invalid action");
//            doGet(req, resp);
//            return;
//        }
//
//        boolean success = feedbackDAO.processFeedback(feedbackId, action);
//        if (success) {
//            req.setAttribute("message", "Feedback " + action + "d successfully");
//        } else {
//            req.setAttribute("error", "Failed to process feedback");
//        }
//
//        doGet(req, resp);
//    }
}