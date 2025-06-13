package dao;

import dto.PostApprovalDTO;
import model.Account;
import model.Post;
import util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Lớp DAO để xử lý phê duyệt bài viết (tương tác với CSDL)
public class PostApprovalDAO {
    // Kết nối tới CSDL
    private final Connection connection;

    // Constructor: khởi tạo kết nối từ DBContext
    public PostApprovalDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }
    }

    /**
     * Lấy danh sách các bài post đang ở trạng thái "pending" (chờ phê duyệt)
     * @param accountId ID của admin đang đăng nhập
     * @return Danh sách PostApprovalDTO
     */
    public List<PostApprovalDTO> getPendingPosts(int accountId) {
        List<PostApprovalDTO> pendingPosts = new ArrayList<>();
        String sql = "SELECT p.post_id, p.post_content, p.account_id, p.post_status, p.created_at, a.username " +
                "FROM post p " +
                "JOIN account a ON p.account_id = a.account_id " +
                "WHERE p.post_status = 'pending' " +
                "AND EXISTS (SELECT 1 FROM account WHERE account_id = ? AND account_role = 'admin')";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId); // Xác thực người gọi là admin

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Tạo đối tượng Post và thiết lập thông tin từ kết quả truy vấn
                    Post post = new Post();
                    post.setId(rs.getInt("post_id"));
                    post.setPostContent(rs.getString("post_content"));
                    post.setPostStatus(rs.getString("post_status"));
                    // post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); // (nếu cần dùng)

                    // Tạo đối tượng Account (người đăng bài)
                    Account account = new Account();
                    account.setId(rs.getInt("account_id"));
                    account.setUsername(rs.getString("username"));

                    // Tạo DTO chứa thông tin bài viết và tài khoản, thêm vào danh sách
                    pendingPosts.add(new PostApprovalDTO(post, account));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error in getPendingPosts: " + e.getMessage());
            e.printStackTrace();
        }

        return pendingPosts;
    }

    /**
     * Xử lý phê duyệt hoặc từ chối một bài viết
     * @param postId ID bài viết
     * @param action hành động ("approve" hoặc "disapprove")
     * @param accountId ID admin thực hiện
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean processPost(int postId, String action, int accountId) {
        Connection conn = null;

        try {
            conn = this.connection;
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Kiểm tra người thực hiện có phải là admin không
            String checkSql = "SELECT 1 FROM account WHERE account_id = ? AND account_role = 'admin'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, accountId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback(); // Không phải admin -> rollback
                        return false;
                    }
                }
            }

            // 2. Cập nhật trạng thái bài viết (approved / rejected)
            String updateSql = "UPDATE post SET post_status = ?, last_updated = ? WHERE post_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, "approved".equalsIgnoreCase(action) ? "approved" : "rejected");
                updateStmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now())); // Thời gian cập nhật
                updateStmt.setInt(3, postId);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback(); // Không cập nhật được -> rollback
                    return false;
                }
            }

            conn.commit(); // Nếu không lỗi, commit transaction
            return true;

        } catch (SQLException e) {
            System.err.println("SQL error in processPost: " + e.getMessage());
            e.printStackTrace();

            // Nếu có lỗi, rollback lại dữ liệu
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
