package controller;

import dao.FeedbackGroupDAO;
import model.ResGroupDTO;
import dao.GroupDAO;
import model.FeedbackGroupDTO;
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

        ResGroupDTO groupInfo = groupDAO.getGroup(groupId);
        if (groupInfo == null) {
            req.setAttribute("groupInfo", null);
            req.setAttribute("error", "Group information not found for groupId: " + groupId);
        } else {
            req.setAttribute("groupInfo", groupInfo);
        }

        // Giả sử không cần GroupDAO riêng, chỉ lấy feedback
        int managerId = 1; // Hardcode để test
        List<FeedbackGroupDTO> feedbacks = feedbackDAO.getFeedbacksByGroupId(groupId, "sent"); // report_status = 1
        req.setAttribute("feedbacks", feedbacks);
        req.setAttribute("groupId", groupId);
        req.setAttribute("csrfToken", UUID.randomUUID().toString());
        req.getSession().setAttribute("csrfToken", req.getAttribute("csrfToken"));

        req.getRequestDispatcher("/WEB-INF/views/viewFeedback.jsp").forward(req, resp);
    }


}