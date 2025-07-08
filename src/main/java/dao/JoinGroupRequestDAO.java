package dao;

import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class JoinGroupRequestDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

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
