package dao;

import dto.MemberViewDTO;
import model.Account;
import model.Participate;
import model.ParticipateId;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO class for handling database operations related to group members.
 */
public class MemberViewDAO {
    private static final Logger LOGGER = Logger.getLogger(MemberViewDAO.class.getName());
    private final Connection connection;

    public MemberViewDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }
    }

    // SQL query to retrieve members from the participate table
//    private static final String SELECT_MEMBERS_BY_GROUP_ID =
//            "SELECT p.account_id AS p_account_id, p.group_id, p.participate_start_date, " +
//                    "       a.account_id AS a_account_id, a.username, a.avatar, a.fullname " +
//                    "FROM participate p " +
//                    "JOIN account a ON p.account_id = a.account_id " +
//                    "WHERE p.group_id = ? " +
//                    "ORDER BY p.participate_start_date DESC";
//
//    /**
//     * Retrieves the list of members for a given group.
//     */
//    public List<MemberViewDTO> getMembersByGroupId(int groupId) {
//        List<MemberViewDTO> members = new ArrayList<>();
//        try (PreparedStatement stmt = connection.prepareStatement(SELECT_MEMBERS_BY_GROUP_ID)) {
//            stmt.setInt(1, groupId);
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    Participate participate = new Participate();
//
//                    // Create and set composite key
//                    ParticipateId id = new ParticipateId();
//                    id.setAccountId(rs.getInt("p_account_id"));
//                    id.setGroupId(rs.getInt("group_id"));
//                    participate.setId(id);
//
//                    participate.setParticipateStartDate(rs.getTimestamp("participate_start_date").toInstant());
//
//                    Account account = new Account();
//                    account.setId(rs.getInt("a_account_id"));
//                    account.setUsername(rs.getString("username"));
//                    account.setAvatar(rs.getString("avatar"));
//                    account.setFullname(rs.getString("fullname"));
//
//                    participate.setAccount(account);
//
//                    MemberViewDTO dto = new MemberViewDTO(participate, account);
//                    members.add(dto);
//                }
//            }
//        } catch (SQLException e) {
//            LOGGER.severe("Error retrieving member list: " + e.getMessage());
//        }
//        return members;
//    }

    // SQL query to remove a member from the participate table
    private static final String DELETE_MEMBER =
            "DELETE FROM participate WHERE account_id = ? AND group_id = ?";

    /**
     * Removes a member from a group.
     */
    public boolean removeMember(int accountId, int groupId) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_MEMBER)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Removed member with accountId " + accountId + " from groupId " + groupId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error removing member: " + e.getMessage());
            return false;
        }
    }

    // SQL query to check manager status
    private static final String CHECK_MANAGER =
            "SELECT COUNT(*) FROM manage WHERE account_id = ? AND group_id = ?";

    /**
     * Checks if an account is a manager of a group.
     */
    public boolean isGroupManager(int accountId, int groupId) {
        try (PreparedStatement stmt = connection.prepareStatement(CHECK_MANAGER)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error checking manager status: " + e.getMessage());
        }
        return false;
    }

    // SQL query to promote a member to manager
    private static final String PROMOTE_TO_MANAGER =
            "INSERT INTO manage (account_id, group_id) VALUES (?, ?)";

    /**
     * Promotes a member to manager.
     */
    public boolean promoteToManager(int accountId, int groupId) {
        // Verify if the account is a member of the group
        if (!isMember(accountId, groupId)) {
            LOGGER.warning("Cannot promote: accountId " + accountId + " is not a member of groupId " + groupId);
            return false;
        }

        // Check if the account is already a manager
        if (isGroupManager(accountId, groupId)) {
            LOGGER.warning("Cannot promote: accountId " + accountId + " is already a manager of groupId " + groupId);
            return false;
        }

        try (PreparedStatement stmt = connection.prepareStatement(PROMOTE_TO_MANAGER)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Promoted accountId " + accountId + " to manager of groupId " + groupId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error promoting member to manager: " + e.getMessage());
            return false;
        }
    }

    // SQL query to check membership
    private static final String CHECK_MEMBER =
            "SELECT COUNT(*) FROM participate WHERE account_id = ? AND group_id = ?";

    /**
     * Checks if an account is a member of a group.
     */
    private boolean isMember(int accountId, int groupId) {
        try (PreparedStatement stmt = connection.prepareStatement(CHECK_MEMBER)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error checking membership: " + e.getMessage());
        }
        return false;
    }
}