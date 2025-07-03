package controller;

import dao.PostApprovalDAO;
import dao.GroupDAO;
import dto.PostApprovalDTO;
import dto.ResGroupDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/approvePost")
public class PostApprovalServlet extends HttpServlet {
    private final PostApprovalDAO postApprovalDAO = new PostApprovalDAO();
    private final GroupDAO groupDAO = new GroupDAO(); // Sử dụng GroupDAO thực tế

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

        // Lấy thông tin nhóm từ cơ sở dữ liệu bằng GroupDAO
        ResGroupDTO groupInfo = groupDAO.getGroup(groupId);
        if (groupInfo == null) {
            req.setAttribute("groupInfo", null);
            req.setAttribute("error", "Group information not found for groupId: " + groupId);
        } else {
            req.setAttribute("groupInfo", groupInfo);
        }

        int managerId = 1; // Hardcode để test
        List<PostApprovalDTO> pendingPosts = postApprovalDAO.getPendingPosts(managerId);
        req.setAttribute("posts", pendingPosts);
        req.setAttribute("groupId", groupId);
        req.setAttribute("csrfToken", UUID.randomUUID().toString());
        req.getSession().setAttribute("csrfToken", req.getAttribute("csrfToken"));

        req.getRequestDispatcher("/WEB-INF/views/postApprove.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int managerId = 1;

        String sessionCsrfToken = (String) req.getSession().getAttribute("csrfToken");
        String requestCsrfToken = req.getParameter("csrfToken");
        if (sessionCsrfToken == null || !sessionCsrfToken.equals(requestCsrfToken)) {
            req.setAttribute("error", "Invalid CSRF token");
            doGet(req, resp);
            return;
        }

        String postIdParam = req.getParameter("postId");
        String action = req.getParameter("action");
        String groupIdParam = req.getParameter("groupId");

        if (postIdParam == null || action == null || groupIdParam == null) {
            req.setAttribute("error", "Post ID, action, and groupId are required");
            doGet(req, resp);
            return;
        }

        int postId;
        try {
            postId = Integer.parseInt(postIdParam);
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Invalid post ID");
            doGet(req, resp);
            return;
        }

        if (!"approve".equalsIgnoreCase(action) && !"disapprove".equalsIgnoreCase(action)) {
            req.setAttribute("error", "Invalid action");
            doGet(req, resp);
            return;
        }

        boolean success = postApprovalDAO.processPost(postId, action);
        if (success) {
            req.setAttribute("message", "Post " + action + "d successfully");
        } else {
            req.setAttribute("error", "Failed to process post");
        }

        doGet(req, resp);
    }
}