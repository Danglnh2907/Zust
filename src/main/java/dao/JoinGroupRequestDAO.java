package dao;

import model.Account;
import model.Group;
import model.JoinGroupRequest;
import model.Participate;
import model.ParticipateId;
import util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JoinGroupRequestDAO {
    // Kết nối đến cơ sở dữ liệu
    private final Connection connection;

    // Constructor: khởi tạo đối tượng DAO với kết nối DB
    public JoinGroupRequestDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            // Nếu không thể kết nối DB, dừng chương trình với lỗi rõ ràng
            throw new IllegalStateException("Database connection failed");
        }
    }

    /**
     * Lấy danh sách các yêu cầu tham gia nhóm đang ở trạng thái "sended"
     *
     * @param groupId ID của nhóm muốn xem các yêu cầu
     * @return List các yêu cầu tham gia nhóm (JoinGroupRequest)
     */
    public List<JoinGroupRequest> getPendingJoinRequests(int groupId) {
        List<JoinGroupRequest> requests = new ArrayList<>();
        String sql = "SELECT join_group_request_id, join_group_request_content, join_group_request_date, " +
                "join_group_request_status, account_id, group_id " +
                "FROM join_group_request " +
                "WHERE group_id = ? AND join_group_request_status = 'sended'";
        System.out.println("==> [DEBUG] Đang truy vấn yêu cầu tham gia nhóm với groupId = " + groupId);


        // database đúng khi groupId = 1

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                // Lặp qua từng bản ghi trả về từ DB
                while (rs.next()) {
                    // Tạo đối tượng JoinGroupRequest và gán dữ liệu từ DB
                    JoinGroupRequest request = new JoinGroupRequest();


                    request.setId(rs.getInt("join_group_request_id"));
                    request.setJoinGroupRequestContent(rs.getString("join_group_request_content"));
//                    request.setJoinGroupRequestDate(rs.getObject("join_group_request_date", Instant.class));
                    request.setJoinGroupRequestStatus(rs.getString("join_group_request_status"));

                    // Tạo đối tượng Account và Group chỉ với ID (đủ dùng cho mục đích hiển thị)
                    Account account = new Account();
                    account.setId(rs.getInt("account_id"));
                    request.setAccount(account);

                    Group group = new Group();
                    group.setId(rs.getInt("group_id"));
                    request.setGroup(group);
                    System.out.println("==> [DEBUG] Lấy được yêu cầu:");
                    System.out.println("    ID: " + request.getId());
                    System.out.println("    Nội dung: " + request.getJoinGroupRequestContent());
                    System.out.println("    Trạng thái: " + request.getJoinGroupRequestStatus());
                    System.out.println("    Account ID: " + request.getAccount().getId());
                    System.out.println("    Group ID: " + request.getGroup().getId());
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            // Xử lý lỗi SQL
            System.err.println("SQL error in getPendingJoinRequests: " + e.getMessage());
            System.err.println("[ERROR] getPendingJoinRequests - SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Phê duyệt một yêu cầu tham gia nhóm: Cập nhật trạng thái, đồng thời thêm vào bảng participate.
     *
     * @param requestId ID của yêu cầu tham gia cần phê duyệt
     * @return true nếu phê duyệt thành công, ngược lại false
     */
    public boolean approveJoinRequest(int requestId) {
        Connection conn = null;
        try {
            conn = this.connection;
            conn.setAutoCommit(false); // Bắt đầu transaction

            System.out.println("[DEBUG] approveJoinRequest - Processing requestId: " + requestId);

            JoinGroupRequest request = getJoinRequestById(requestId);
            System.out.println("[DEBUG] approveJoinRequest - Request found: " + (request != null));
            if (request == null) {
                conn.rollback();
                System.out.println("[ERROR] approveJoinRequest - Request not found for ID: " + requestId);
                return false;
            }

            // Kiểm tra trạng thái hiện tại (nếu cần)
            String currentStatus = request.getJoinGroupRequestStatus();
            System.out.println("[DEBUG] Current status = " + currentStatus);
            if (!"sended".equalsIgnoreCase(currentStatus)) {
                conn.rollback();
                System.out.println("[ERROR] approveJoinRequest - Only 'sended' requests can be approved.");
                return false;
            }

            // Bước 1: Cập nhật trạng thái
            String updateSql = "UPDATE join_group_request SET join_group_request_status = 'accepted' WHERE join_group_request_id = ?";
            try (PreparedStatement stmt1 = conn.prepareStatement(updateSql)) {
                stmt1.setInt(1, requestId);
                int rowsUpdated = stmt1.executeUpdate();
                System.out.println("[DEBUG] approveJoinRequest - Rows updated: " + rowsUpdated);
                if (rowsUpdated == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Bước 2: Chèn vào participate
            String insertSql = "INSERT INTO participate (account_id, group_id, participate_start_date) VALUES (?, ?, ?)";
            try (PreparedStatement stmt2 = conn.prepareStatement(insertSql)) {
                stmt2.setInt(1, request.getAccount().getId());
                stmt2.setInt(2, request.getGroup().getId());
                stmt2.setTimestamp(3, java.sql.Timestamp.from(Instant.now()));
                stmt2.executeUpdate();
                System.out.println("[DEBUG] approveJoinRequest - Inserted into participate");
            }

            conn.commit(); // Giao dịch thành công
            System.out.println("[DEBUG] approveJoinRequest - Transaction committed");
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Rollback nếu lỗi
            } catch (SQLException ex) {
                System.err.println("[ERROR] Failed to rollback: " + ex.getMessage());
            }
            System.err.println("[ERROR] approveJoinRequest - SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true); // Reset lại chế độ auto-commit
            } catch (SQLException e) {
                System.err.println("[ERROR] Failed to reset auto-commit: " + e.getMessage());
            }
        }
    }


    /**
     * Từ chối một yêu cầu tham gia nhóm.
     *
     * @param requestId ID yêu cầu cần từ chối
     * @return true nếu cập nhật thành công
     */
    public boolean disapproveJoinRequest(int requestId) {
        System.out.println("==> [DEBUG] Từ chối requestId = " + requestId);

        String sql = "UPDATE join_group_request SET join_group_request_status = 'rejected' WHERE join_group_request_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0; // Trả về true nếu có ít nhất 1 dòng bị thay đổi

        } catch (SQLException e) {
            System.err.println("SQL error in disapproveJoinRequest: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy thông tin chi tiết của một yêu cầu tham gia theo ID.
     * Phục vụ cho cả việc hiển thị hoặc kiểm tra trước khi approve.
     *
     * @param requestId ID của yêu cầu
     * @return JoinGroupRequest hoặc null nếu không tìm thấy
     */
    private JoinGroupRequest getJoinRequestById(int requestId) {
        System.out.println("==> [DEBUG] Lấy chi tiết yêu cầu theo ID = " + requestId);

        String sql = "SELECT join_group_request_id, join_group_request_content, join_group_request_date, " +
                "join_group_request_status, account_id, group_id " +
                "FROM join_group_request WHERE join_group_request_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JoinGroupRequest request = new JoinGroupRequest();
                    request.setId(rs.getInt("join_group_request_id"));
                    request.setJoinGroupRequestContent(rs.getString("join_group_request_content"));
//                    request.setJoinGroupRequestDate(rs.getObject("join_group_request_date", Instant.class));
                    request.setJoinGroupRequestStatus(rs.getString("join_group_request_status"));

                    Account account = new Account();
                    account.setId(rs.getInt("account_id"));
                    request.setAccount(account);

                    Group group = new Group();
                    group.setId(rs.getInt("group_id"));
                    request.setGroup(group);

                    return request;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error in getJoinRequestById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
