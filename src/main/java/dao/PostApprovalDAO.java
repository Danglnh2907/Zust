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
     * Lấy danh sách các bài post đang ở trạng thái "sended" (chờ phê duyệt)
     * @param managerId ID của admin đang đăng nhập
     * @return Danh sách PostApprovalDTO
     */
    public List<PostApprovalDTO> getPendingPosts(int managerId) {
        List<PostApprovalDTO> pendingPosts = new ArrayList<>();
        String sql = "SELECT p.post_id, p.post_content, p.post_status " +
                "FROM post p " +
                "JOIN manage m ON p.group_id = m.group_id " +
                "WHERE p.post_status = 'sended' AND m.account_id = ?";
        System.out.println("Executing SQL: " + sql + " with managerId = " + managerId);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, managerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Found record: post_id = " + rs.getInt("post_id") +
                            ", post_content = " + rs.getString("post_content") +
                            ", post_status = " + rs.getString("post_status"));
                    Post post = new Post();
                    post.setId(rs.getInt("post_id"));
                    post.setPostContent(rs.getString("post_content"));
                    post.setPostStatus(rs.getString("post_status"));
                    pendingPosts.add(new PostApprovalDTO(post));
                }
                System.out.println("Total pending posts found: " + pendingPosts.size());
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return pendingPosts;
    }

    /**
     * Xử lý phê duyệt hoặc từ chối một bài viết
     * @param postId ID bài viết
     * @param action hành động ("approve" hoặc "disapprove")
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean processPost(int postId, String action) {
        Connection conn = null; // Khởi tạo biến kết nối

        try {
            conn = this.connection; // Lấy kết nối hiện tại
            conn.setAutoCommit(false); // Tắt auto-commit để bắt đầu transaction (giao dịch)

            // =============================
            // 1. Kiểm tra quyền người xử lý
            // =============================

            // SQL kiểm tra xem accountId có phải là người quản lý group chứa bài post hay không
//            String checkSql = "SELECT 1 FROM manage m " +
//                    "JOIN post p ON m.group_id = p.group_id " +
//                    "WHERE p.post_id = ? AND m.account_id = ?";
//
//            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
//                checkStmt.setInt(1, postId); // Gán postId vào câu truy vấn
//                checkStmt.setInt(2, accountId); // Gán accountId (người kiểm duyệt)
//
//                try (ResultSet rs = checkStmt.executeQuery()) {
//                    if (!rs.next()) {
//                        // Không có dòng nào khớp → không phải người quản lý group → rollback và trả false
//                        conn.rollback();
//                        return false;
//                    }
//                }
//            }

            // =======================================
            // 2. Cập nhật trạng thái bài viết (post)
            // =======================================

            // Câu SQL cập nhật trạng thái của bài viết
            String updateSql = "UPDATE post SET post_status = ?WHERE post_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                // Xác định trạng thái cần cập nhật: approved hoặc rejected
                updateStmt.setString(1, "published".equalsIgnoreCase(action) ? "published" : "rejected");

                // Cập nhật thời gian chỉnh sửa gần nhất
//                updateStmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));

                // Gán postId cho điều kiện WHERE
                updateStmt.setInt(2, postId);

                // Thực hiện truy vấn cập nhật
                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected == 0) {
                    // Không có dòng nào bị ảnh hưởng → cập nhật thất bại → rollback và trả false
                    conn.rollback();
                    return false;
                }
            }

            conn.commit(); // Nếu không có lỗi, commit giao dịch để lưu thay đổi vào DB
            return true;   // Thành công

        } catch (SQLException e) {
            // Nếu có lỗi xảy ra trong quá trình xử lý SQL
            System.err.println("SQL error in processPost: " + e.getMessage());
            e.printStackTrace();

            // Thử rollback nếu đang trong một transaction
            if (conn != null) {
                try {
                    conn.rollback(); // Khôi phục lại dữ liệu như trước khi giao dịch bắt đầu
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }

            return false; // Trả về false nếu có lỗi
        }
    }


}
