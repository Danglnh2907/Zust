package controller;

import dao.PostApprovalDAO;
import dto.PostApprovalDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/approvePost")
public class PostApprovalServlet extends HttpServlet {
    private final PostApprovalDAO postApprovalDAO = new PostApprovalDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
        String accountRole = (String) req.getSession().getAttribute("accountRole");
        if (accountId == null || !"admin".equals(accountRole)) {
            accountId = 1003; // Set tạm accountId để test
            req.getSession().setAttribute("accountId", accountId);
            req.getSession().setAttribute("accountRole", "admin"); // Set tạm role để test
            System.out.println("Set temporary accountId: " + accountId + " and role: admin for testing");
        }

        List<PostApprovalDTO> pendingPosts = postApprovalDAO.getPendingPosts(accountId);
        if (pendingPosts.isEmpty()) {
            req.setAttribute("message", "No pending posts to approve");
        } else {
            req.setAttribute("posts", pendingPosts);
        }
        req.getRequestDispatcher("/WEB-INF/views/postApproval.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
        String accountRole = (String) req.getSession().getAttribute("accountRole");
        if (accountId == null || !"admin".equals(accountRole)) {
            resp.sendRedirect("login.jsp");
            return;
        }

        String postIdParam = req.getParameter("postId");
        String action = req.getParameter("action");
        if (postIdParam == null || postIdParam.trim().isEmpty() || action == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID and action are required");
            return;
        }

        int postId;
        try {
            postId = Integer.parseInt(postIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid post ID");
            return;
        }

        if (!"approve".equalsIgnoreCase(action) && !"disapprove".equalsIgnoreCase(action)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            return;
        }

        boolean success = postApprovalDAO.processPost(postId, action, accountId);
        if (success) {
            req.setAttribute("message", "Post " + action + "d successfully");
        } else {
            req.setAttribute("error", "Failed to process post");
        }
        resp.sendRedirect(req.getContextPath() + "/approvePost");
    }
}