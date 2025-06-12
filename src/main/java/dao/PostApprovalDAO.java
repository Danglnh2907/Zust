package dao;

import dto.PostApprovalDTO;
import model.Account;
import model.Post;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostApprovalDAO {
    private final Connection connection;

    public PostApprovalDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }
    }

    public List<PostApprovalDTO> getPendingPosts(int accountId) {
        List<PostApprovalDTO> pendingPosts = new ArrayList<>();
        String sql = "SELECT p.post_id, p.post_content, p.account_id, p.post_status, p.created_at, a.username " +
                "FROM post p " +
                "JOIN account a ON p.account_id = a.account_id " +
                "WHERE p.post_status = 'pending' " +
                "AND EXISTS (SELECT 1 FROM account WHERE account_id = ? AND account_role = 'admin')";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("post_id"));
                    post.setPostContent(rs.getString("post_content"));
                    post.setPostStatus(rs.getString("post_status"));
//                    post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                    Account account = new Account();
                    account.setId(rs.getInt("account_id"));
                    account.setUsername(rs.getString("username"));

                    pendingPosts.add(new PostApprovalDTO(post, account));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error in getPendingPosts: " + e.getMessage());
            e.printStackTrace();
        }
        return pendingPosts;
    }

    public boolean processPost(int postId, String action, int accountId) {
        Connection conn = null;
        try {
            conn = this.connection;
            conn.setAutoCommit(false);

            // Kiểm tra quyền admin
            String checkSql = "SELECT 1 FROM account WHERE account_id = ? AND account_role = 'admin'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, accountId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Cập nhật trạng thái bài đăng
            String updateSql = "UPDATE post SET post_status = ?, last_updated = ? WHERE post_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, "approved".equalsIgnoreCase(action) ? "approved" : "rejected");
                updateStmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                updateStmt.setInt(3, postId);
                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("SQL error in processPost: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            return false;
        }
    }
}