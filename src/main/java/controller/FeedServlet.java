package controller;

import dao.LikePostDAO;
import dao.RepostDAO;
import dto.PostWithLikeDTO;
import dto.PostWithRepostDTO;
import model.Account;
import model.Post;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.DBContext;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/feed")
public class FeedServlet extends HttpServlet {
    private final LikePostDAO likePostDAO = new LikePostDAO();
    private final RepostDAO repostDAO = new RepostDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
        System.out.println("FeedServlet - Session accountId: " + accountId);

        // cho account thu de test
        if (accountId == null) {
            accountId = 1003; // Set tạm accountId = 1003 (John Doe)
            req.getSession().setAttribute("accountId", accountId);
            System.out.println("Set temporary accountId: " + accountId + " for testing");
        }

        List<Object> posts = new ArrayList<>(); // Sử dụng danh sách chung cho like và repost
        Connection connection = new DBContext().getConnection();
        if (connection == null) {
            System.out.println("Database connection is null");
            req.setAttribute("error", "Failed to connect to database");
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
            return;
        }

        String sql = "SELECT post_id, post_content, account_id FROM post WHERE post_status = 'published'";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("post_id"));
                post.setPostContent(rs.getString("post_content"));
                Account account = new Account();
                account.setId(rs.getInt("account_id"));
                post.setAccount(account);

                int likeCount = likePostDAO.getLikeCount(post.getId());
                boolean isLiked = (accountId != null) ? likePostDAO.isPostLiked(accountId, post.getId()) : false;
                PostWithLikeDTO likeDto = new PostWithLikeDTO(post, likeCount, isLiked);
                posts.add(likeDto);

                int repostCount = repostDAO.getRepostCount(post.getId());
                boolean isReposted = (accountId != null) ? repostDAO.isPostReposted(accountId, post.getId()) : false;
                PostWithRepostDTO repostDto = new PostWithRepostDTO(post, repostCount, isReposted);
                posts.add(repostDto);
            }
        } catch (SQLException e) {
            System.err.println("SQL error in FeedServlet: " + e.getMessage());
            e.printStackTrace();
            req.setAttribute("error", "Failed to load posts: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
            return;
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }

        req.setAttribute("posts", posts);
        req.getRequestDispatcher("/WEB-INF/views/LikePost.jsp").forward(req, resp);
    }
}