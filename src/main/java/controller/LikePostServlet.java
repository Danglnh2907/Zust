package controller;

import dao.LikePostDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;


@WebServlet("/likePost")
public class LikePostServlet extends HttpServlet {
    private final LikePostDAO likePostDAO = new LikePostDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
        System.out.println("LikePostServlet - Session accountId: " + accountId);
        if (accountId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"User not logged in\", \"status\": 401}");
            return;
        }

        int postId;
        try {
            postId = Integer.parseInt(req.getParameter("postId"));
            System.out.println("Received postId: " + postId);
        } catch (NumberFormatException e) {
            System.err.println("Invalid postId format: " + req.getParameter("postId"));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid postId format\", \"status\": 400}");
            return;
        }

        boolean isLiked = likePostDAO.isPostLiked(accountId, postId);
        boolean success = false;

        if (isLiked) {
            success = likePostDAO.unlikePost(accountId, postId);
            System.out.println("Unliking post " + postId);
        } else {
            success = likePostDAO.likePost(accountId, postId);
            System.out.println("Liking post " + postId);
        }

        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {
            int likeCount = likePostDAO.getLikeCount(postId);
            if (success) {
                out.write("{\"likeCount\": " + likeCount + ", \"isLiked\": " + !isLiked + ", \"status\": 200}");
            } else {
                out.write("{\"error\": \"Failed to toggle like\", \"likeCount\": " + likeCount + ", \"status\": 400}");
            }
            out.flush();
        } catch (IOException e) {
            System.err.println("Error writing JSON response: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error sending response");
        }
    }
}