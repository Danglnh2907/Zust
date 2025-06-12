package dao;

import model.Account;
import model.Group;
import model.JoinGroupRequest;
import model.Participate;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JoinGroupRequestDAO {
    private final Connection connection;

    public JoinGroupRequestDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }
    }

    public List<JoinGroupRequest> getPendingJoinRequests(int groupId, int accountId) {
        List<JoinGroupRequest> requests = new ArrayList<>();
        String sql = "SELECT jgr.join_group_request_id, jgr.join_group_request_content, jgr.join_group_request_date, " +
                "jgr.join_group_request_status, jgr.account_id, jgr.group_id " +
                "FROM join_group_request jgr " +
                "WHERE jgr.group_id = ? AND jgr.join_group_request_status = 'sended' " +
                "AND EXISTS (SELECT 1 FROM [group] g WHERE g.group_id = jgr.group_id AND (g.created_by = ? OR ? IN (SELECT account_id FROM group_member WHERE group_id = jgr.group_id AND is_admin = 1)))";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, accountId);
            stmt.setInt(3, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JoinGroupRequest request = new JoinGroupRequest();
                    request.setId(rs.getInt("join_group_request_id"));
                    request.setJoinGroupRequestContent(rs.getString("join_group_request_content"));
                    request.setJoinGroupRequestDate(rs.getObject("join_group_request_date", Instant.class));
                    request.setJoinGroupRequestStatus(rs.getString("join_group_request_status"));
                    Account account = new Account();
                    account.setId(rs.getInt("account_id"));
                    request.setAccount(account);
                    Group group = new Group();
                    group.setId(rs.getInt("group_id"));
                    request.setGroup(group);
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error in getPendingJoinRequests: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

    public boolean approveJoinRequest(int requestId, int accountId) {
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        try {
            conn = this.connection; // Sử dụng connection từ constructor
            conn.setAutoCommit(false);

            // Lấy thông tin yêu cầu
            JoinGroupRequest request = getJoinRequestById(requestId);
            if (request == null) {
                conn.rollback();
                return false;
            }

            int groupId = request.getGroup().getId();
            // Kiểm tra quyền
            String checkSql = "SELECT 1 FROM [group] WHERE group_id = ? AND (created_by = ? OR ? IN (SELECT account_id FROM group_member WHERE group_id = ? AND is_admin = 1))";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, groupId);
                checkStmt.setInt(2, accountId);
                checkStmt.setInt(3, accountId);
                checkStmt.setInt(4, groupId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Cập nhật trạng thái yêu cầu
            String updateSql = "UPDATE join_group_request SET join_group_request_status = 'approved' WHERE join_group_request_id = ?";
            stmt1 = conn.prepareStatement(updateSql);
            stmt1.setInt(1, requestId);
            int rowsUpdated = stmt1.executeUpdate();
            if (rowsUpdated == 0) {
                conn.rollback();
                return false;
            }

            // Thêm vào participate
            String insertSql = "INSERT INTO participate (account_id, group_id, participate_start_date) VALUES (?, ?, ?)";
            stmt2 = conn.prepareStatement(insertSql);
            stmt2.setInt(1, request.getAccount().getId());
            stmt2.setInt(2, request.getGroup().getId());
            stmt2.setTimestamp(3, java.sql.Timestamp.from(Instant.now()));
            stmt2.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("SQL error in approveJoinRequest: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            try {
                if (stmt1 != null) stmt1.close();
                if (stmt2 != null) stmt2.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean disapproveJoinRequest(int requestId, int accountId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.connection;
            conn.setAutoCommit(false);

            // Kiểm tra quyền
            JoinGroupRequest request = getJoinRequestById(requestId);
            if (request == null) {
                return false;
            }
            int groupId = request.getGroup().getId();
            String checkSql = "SELECT 1 FROM [group] WHERE group_id = ? AND (created_by = ? OR ? IN (SELECT account_id FROM group_member WHERE group_id = ? AND is_admin = 1))";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, groupId);
                checkStmt.setInt(2, accountId);
                checkStmt.setInt(3, accountId);
                checkStmt.setInt(4, groupId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                }
            }

            // Cập nhật trạng thái
            String updateSql = "UPDATE join_group_request SET join_group_request_status = 'rejected' WHERE join_group_request_id = ?";
            stmt = conn.prepareStatement(updateSql);
            stmt.setInt(1, requestId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            System.err.println("SQL error in disapproveJoinRequest: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private JoinGroupRequest getJoinRequestById(int requestId) {
        String sql = "SELECT jgr.join_group_request_id, jgr.join_group_request_content, jgr.join_group_request_date, " +
                "jgr.join_group_request_status, jgr.account_id, jgr.group_id " +
                "FROM join_group_request jgr WHERE jgr.join_group_request_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JoinGroupRequest request = new JoinGroupRequest();
                    request.setId(rs.getInt("join_group_request_id"));
                    request.setJoinGroupRequestContent(rs.getString("join_group_request_content"));
                    request.setJoinGroupRequestDate(rs.getObject("join_group_request_date", Instant.class));
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