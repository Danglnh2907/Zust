package dao;

import dto.JoinGroupRequestDTO;
import model.Account;
import model.JoinGroupRequest;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JoinGroupRequestDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final Connection connection;

    public JoinGroupRequestDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }
    }

    private static final String SELECT_REQUESTS_BY_GROUP_ID =
            "SELECT jgr.join_group_request_id, jgr.join_group_request_content, jgr.join_group_request_date, " +
                    "jgr.join_group_request_status, a.account_id, a.username, a.avatar, a.fullname " +
                    "FROM join_group_request jgr " +
                    "JOIN account a ON jgr.account_id = a.account_id " +
                    "WHERE jgr.group_id = ? AND jgr.join_group_request_status = 'sent' " +
                    "ORDER BY jgr.join_group_request_date DESC";

    public List<JoinGroupRequestDTO> getRequestsByGroupId(int groupId) {
        List<JoinGroupRequestDTO> requests = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_REQUESTS_BY_GROUP_ID)) {
            stmt.setInt(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JoinGroupRequest request = new JoinGroupRequest();
                    request.setId(rs.getInt("join_group_request_id"));
                    request.setJoinGroupRequestContent(rs.getString("join_group_request_content"));
//                    request.setJoinGroupRequestDate(rs.getObject("join_group_request_date", LocalDateTime.class));
                    request.setJoinGroupRequestStatus(rs.getString("join_group_request_status"));

                    Account account = new Account();
                    account.setId(rs.getInt("account_id"));
                    account.setUsername(rs.getString("username"));
                    account.setAvatar(rs.getString("avatar"));
                    account.setFullname(rs.getString("fullname"));

                    JoinGroupRequestDTO dto = new JoinGroupRequestDTO(request, account);
                    requests.add(dto);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error fetching join requests: " + e.getMessage());
        }
        return requests;
    }

    public boolean processRequest(int requestId, String action) {
        String updateRequestSql = "UPDATE join_group_request SET join_group_request_status = ? WHERE join_group_request_id = ?";
        String insertParticipateSql = "INSERT INTO participate (account_id, group_id, participate_start_date) VALUES (?, ?, GETDATE())";
        try (Connection conn = new DBContext().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement updateStmt = conn.prepareStatement(updateRequestSql)) {
                String newStatus = "approve".equalsIgnoreCase(action) ? "accepted" : "rejected";
                updateStmt.setString(1, newStatus);
                updateStmt.setInt(2, requestId);
                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }

                if ("approve".equalsIgnoreCase(action)) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertParticipateSql)) {
                        // Lấy account_id và group_id từ join_group_request
                        String getRequestDetailsSql = "SELECT account_id, group_id FROM join_group_request WHERE join_group_request_id = ?";
                        try (PreparedStatement getStmt = conn.prepareStatement(getRequestDetailsSql)) {
                            getStmt.setInt(1, requestId);
                            try (ResultSet rs = getStmt.executeQuery()) {
                                if (rs.next()) {
                                    int accountId = rs.getInt("account_id");
                                    int groupId = rs.getInt("group_id");
                                    insertStmt.setInt(1, accountId);
                                    insertStmt.setInt(2, groupId);
                                    insertStmt.executeUpdate();
                                } else {
                                    conn.rollback();
                                    return false;
                                }
                            }
                        }
                    }
                }

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Error processing join request: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.severe("Rollback failed: " + ex.getMessage());
            }
            return false;
        }
    }

    public boolean joinGroup(int accountId, String content, int groupId) {

        logger.info("Attempting to create join request for account ID: " + accountId + " and group ID: " + groupId);

        String sql = "INSERT INTO join_group_request (account_id, group_id, join_group_request_content, join_group_request_status) " +
                     "VALUES (?, ?, ?, 'sent')";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            stmt.setString(3, content);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warning("Failed to create join request for account ID: " + accountId + " and group ID: " + groupId);
                return false;
            }
            logger.info("Successfully created join request for account ID: " + accountId + " and group ID: " + groupId);
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to create join request for account ID: " + accountId + " and group ID: " + groupId + " - Error: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelJoinGroup(int accountId, int groupId) {
        logger.info("Attempting to cancel join request for account ID: " + accountId + " and group ID: " + groupId);

        String sql = "DELETE FROM join_group_request WHERE account_id = ? AND group_id = ? AND join_group_request_status = 'sent'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warning("No pending join request found for account ID: " + accountId + " and group ID: " + groupId);
                return false;
            }
            logger.info("Successfully canceled join request for account ID: " + accountId + " and group ID: " + groupId);
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to cancel join request for account ID: " + accountId + " and group ID: " + groupId + " - Error: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        JoinGroupRequestDAO dao = new JoinGroupRequestDAO();
        System.out.println(dao.joinGroup(2, "hatarakimasu", 1));
    }
}