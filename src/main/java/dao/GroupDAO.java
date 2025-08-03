package dao;

import model.*;
import util.database.DBContext;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;


public class GroupDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /*=== User section ===*/

    public boolean createGroup(ReqGroupDTO group) {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);
            String groupSql = "INSERT INTO [group](group_name, group_description, group_cover_image, account_id) VALUES\n" +
                             "(?, ?, ?, ?)";
            PreparedStatement groupSt = conn.prepareStatement(groupSql, PreparedStatement.RETURN_GENERATED_KEYS);
            groupSt.setString(1, group.getGroupName());
            groupSt.setString(2, group.getGroupDescription());
            groupSt.setString(3, group.getCoverImage() != null ? group.getCoverImage() : "cover.jpg");
            groupSt.setInt(4, group.getManagerId());
            int affectedRow = groupSt.executeUpdate();
            if (affectedRow == 0) {
                conn.rollback();
                return false;
            }

            ResultSet rs = groupSt.getGeneratedKeys();
            int groupId;
            if (rs.next()) {
                groupId = rs.getInt(1);
            } else {
                conn.rollback();
                return false;
            }

            int manager = group.getManagerId();

            String participateSql = "INSERT INTO participate(group_id, account_id) VALUES(?, ?)";
            PreparedStatement participateSt = conn.prepareStatement(participateSql);
            participateSt.setInt(1, groupId);
            participateSt.setInt(2, manager);
            affectedRow = participateSt.executeUpdate();
            if (affectedRow == 0) {
                conn.rollback();
                return false;
            }

            String manageSql = "INSERT INTO manage(group_id, account_id) VALUES(?, ?)";
            PreparedStatement manageSt = conn.prepareStatement(manageSql);
            manageSt.setInt(1, groupId);
            manageSt.setInt(2, manager);

            affectedRow = manageSt.executeUpdate();
            if (affectedRow == 0) {
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public InteractGroupDTO getGroup(int accountId, int groupId) {
        logger.info("Retrieving group with ID: " + groupId + " for account ID: " + accountId);
        InteractGroupDTO groupDTO = null;
        String sql = "SELECT g.group_id, g.group_name, g.group_cover_image, g.group_description, g.account_id AS creater_id, g.group_create_date, g.group_status, " +
                     "(SELECT COUNT(*) FROM participate p WHERE p.group_id = g.group_id) AS member_count, " +
                     "(SELECT COUNT(*) FROM post p WHERE p.group_id = g.group_id AND p.post_status = 'published') AS post_count, " +
                     "CASE " +
                     "    WHEN g.account_id = ? THEN 'LEADER' " +
                     "    WHEN EXISTS (SELECT 1 FROM manage m WHERE m.group_id = g.group_id AND m.account_id = ?) THEN 'MANAGER' " +
                     "    WHEN EXISTS (SELECT 1 FROM participate p WHERE p.group_id = g.group_id AND p.account_id = ?) THEN 'JOINED' " +
                     "    WHEN EXISTS (SELECT 1 FROM join_group_request jgr WHERE jgr.group_id = g.group_id AND jgr.account_id = ? AND jgr.join_group_request_status = 'sent') THEN 'SENT' " +
                     "    ELSE 'UNJOINED' " +
                     "END AS interact_status " +
                     "FROM [group] g WHERE g.group_id = ? AND g.group_status = 'active'";


        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId); // For LEADER check
            stmt.setInt(2, accountId); // For MANAGER check
            stmt.setInt(3, accountId); // For JOINED check
            stmt.setInt(4, accountId); // For SENT check
            stmt.setInt(5, groupId);        // For group_id filter
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    groupDTO = new InteractGroupDTO();
                    groupDTO.setId(rs.getInt("group_id"));
                    groupDTO.setName(rs.getString("group_name"));
                    groupDTO.setCoverImage(rs.getString("group_cover_image"));
                    groupDTO.setDescription(rs.getString("group_description"));
                    groupDTO.setCreaterId(rs.getInt("creater_id"));
                    groupDTO.setCreateDate(rs.getTimestamp("group_create_date").toLocalDateTime());
                    groupDTO.setStatus(rs.getString("group_status"));
                    groupDTO.setMemberCount(rs.getInt("member_count"));
                    groupDTO.setPostCount(rs.getInt("post_count"));
                    groupDTO.setInteractStatus(InteractGroupDTO.InteractStatus.valueOf(rs.getString("interact_status")));
                    logger.info("Successfully retrieved group with ID: " + groupId);
                } else {
                    logger.warning("No group found with ID: " + groupId);
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to retrieve group with ID: " + groupId + " - Error: " + e.getMessage());
        }

        return groupDTO;
    }

    public List<InteractGroupDTO> getJoinedGroups(int accountId) {
        logger.info("Retrieving joined groups for account ID: " + accountId);
        List<InteractGroupDTO> groupList = new ArrayList<>();
        String sql = "SELECT g.group_id, g.group_name, g.group_cover_image, g.group_description, g.account_id AS creater_id, g.group_create_date, g.group_status, " +
                     "(SELECT COUNT(*) FROM participate p WHERE p.group_id = g.group_id) AS member_count, " +
                     "(SELECT COUNT(*) FROM post p WHERE p.group_id = g.group_id AND p.post_status = 'published') AS post_count, " +
                     "CASE " +
                     "    WHEN g.account_id = ? THEN 'LEADER' " +
                     "    WHEN EXISTS (SELECT 1 FROM manage m WHERE m.group_id = g.group_id AND m.account_id = ?) THEN 'MANAGER' " +
                     "    WHEN EXISTS (SELECT 1 FROM participate p WHERE p.group_id = g.group_id AND p.account_id = ?) THEN 'JOINED' " +
                     "    ELSE 'UNJOINED' " + // Should not occur due to JOIN conditions
                     "END AS interact_status " +
                     "FROM [group] g " +
                     "WHERE g.group_status = 'active' AND EXISTS (SELECT 1 FROM participate p WHERE p.group_id = g.group_id AND p.account_id = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId); // For LEADER check in CASE
            stmt.setInt(2, accountId); // For MANAGER check in CASE
            stmt.setInt(3, accountId); // For JOINED check in CASE
            stmt.setInt(4, accountId); // For JOINED check in WHERE
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InteractGroupDTO groupDTO = new InteractGroupDTO();
                    groupDTO.setId(rs.getInt("group_id"));
                    groupDTO.setName(rs.getString("group_name"));
                    groupDTO.setCoverImage(rs.getString("group_cover_image"));
                    groupDTO.setDescription(rs.getString("group_description"));
                    groupDTO.setCreaterId(rs.getInt("creater_id"));
                    groupDTO.setCreateDate(rs.getTimestamp("group_create_date").toLocalDateTime());
                    groupDTO.setStatus(rs.getString("group_status"));
                    groupDTO.setMemberCount(rs.getInt("member_count"));
                    groupDTO.setPostCount(rs.getInt("post_count"));
                    groupDTO.setInteractStatus(InteractGroupDTO.InteractStatus.valueOf(rs.getString("interact_status")));

                    groupList.add(groupDTO);
                }
            }
            logger.info("Successfully retrieved " + groupList.size() + " joined groups for account ID: " + accountId);
        } catch (SQLException e) {
            logger.severe("Failed to retrieve joined groups for account ID: " + accountId + " - Error: " + e.getMessage());
        }

        return groupList;
    }

    public ResGroupDTO getGroup(int groupId) {
        String sql = """
                SELECT [group].*, number_of_participant, number_of_post, account.* FROM [group]
                LEFT JOIN\s
                (SELECT group_id, COUNT(*) AS number_of_participant FROM participate\s
                JOIN account ON account.account_id = participate.account_id
                WHERE account.account_status = 'active'
                GROUP BY participate.group_id) AS participant
                ON [group].group_id = participant.group_id
                LEFT JOIN
                (SELECT group_id, COUNT(*) AS number_of_post FROM post
                WHERE post.post_status = 'publish'
                GROUP BY post.group_id) AS post
                ON post.group_id = [group].group_id
                LEFT JOIN manage ON [group].group_id = manage.group_id
                LEFT JOIN account ON manage.account_id = account.account_id
                WHERE [group].group_id = ? AND NOT group_status = 'deleted'""";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ResGroupDTO group = new ResGroupDTO();
                group.setId(rs.getInt("group_id"));
                group.setName(rs.getString("group_name"));
                group.setImage(rs.getString("group_cover_image"));
                group.setDescription(rs.getString("group_description"));
                group.setCreateDate(rs.getTimestamp("group_create_date") != null
                        ? rs.getTimestamp("group_create_date").toLocalDateTime() : null);
                group.setStatus(rs.getString("group_status"));
                group.setNumberParticipants(rs.getInt("number_of_participant"));
                group.setNumberPosts(rs.getInt("number_of_post"));
                do{
                    if (rs.getInt("account_id") != 0 && rs.getString("account_status").equals("active")) {
                        Account account = new Account();
                        account.setId(rs.getInt("account_id"));
                        account.setUsername(rs.getString("username"));
                        account.setAvatar(rs.getString("avatar"));
                        account.setFullname(rs.getString("fullname"));
                        group.addManager(account);
                    }
                }while (rs.next());
                return group;
            }

            return null;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    public List<InteractGroupDTO> getAllGroups(int accountId) {
        logger.info("Retrieving all groups for account ID: " + accountId);
        List<InteractGroupDTO> groupList = new ArrayList<>();
        String sql = "SELECT g.group_id, g.group_name, g.group_cover_image, g.group_description, g.account_id AS creater_id, g.group_create_date, g.group_status, " +
                     "(SELECT COUNT(*) FROM participate p WHERE p.group_id = g.group_id) AS member_count, " +
                     "(SELECT COUNT(*) FROM post p WHERE p.group_id = g.group_id AND p.post_status = 'published') AS post_count, " +
                     "CASE " +
                     "    WHEN g.account_id = ? THEN 'LEADER' " +
                     "    WHEN EXISTS (SELECT 1 FROM manage m WHERE m.group_id = g.group_id AND m.account_id = ?) THEN 'MANAGER' " +
                     "    WHEN EXISTS (SELECT 1 FROM participate p WHERE p.group_id = g.group_id AND p.account_id = ?) THEN 'JOINED' " +
                     "    WHEN EXISTS (SELECT 1 FROM join_group_request jgr WHERE jgr.group_id = g.group_id AND jgr.account_id = ? AND jgr.join_group_request_status = 'sent') THEN 'SENT' " +
                     "    ELSE 'UNJOINED' " +
                     "END AS interact_status " +
                     "FROM [group] g " +
                     "WHERE g.group_status = 'active'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId); // For LEADER check
            stmt.setInt(2, accountId); // For MANAGER check
            stmt.setInt(3, accountId); // For JOINED check
            stmt.setInt(4, accountId); // For SENT check
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InteractGroupDTO groupDTO = new InteractGroupDTO();
                    groupDTO.setId(rs.getInt("group_id"));
                    groupDTO.setName(rs.getString("group_name"));
                    groupDTO.setCoverImage(rs.getString("group_cover_image"));
                    groupDTO.setDescription(rs.getString("group_description"));
                    groupDTO.setCreaterId(rs.getInt("creater_id"));
                    groupDTO.setCreateDate(rs.getTimestamp("group_create_date").toLocalDateTime());
                    groupDTO.setStatus(rs.getString("group_status"));
                    groupDTO.setMemberCount(rs.getInt("member_count"));
                    groupDTO.setPostCount(rs.getInt("post_count"));
                    groupDTO.setInteractStatus(InteractGroupDTO.InteractStatus.valueOf(rs.getString("interact_status")));

                    groupList.add(groupDTO);
                }
            }
            logger.info("Successfully retrieved " + groupList.size() + " groups for account ID: " + accountId);
        } catch (SQLException e) {
            logger.severe("Failed to retrieve groups for account ID: " + accountId + " - Error: " + e.getMessage());
        }
        return groupList;
    }

    public List<MemberDTO> getMembers(int accountId, int groupId) {
        logger.info("Retrieving members for group ID: " + groupId + " for account ID: " + accountId);
        List<MemberDTO> memberList = new ArrayList<>();
        String sql = """
                SELECT a.account_id, a.username, a.avatar, p.participate_start_date \
                FROM participate p \
                JOIN account a ON p.account_id = a.account_id \
                WHERE p.group_id = ? AND a.account_status = 'active' \
                AND NOT EXISTS (SELECT 1 FROM manage m WHERE m.group_id = p.group_id AND m.account_id = p.account_id)""";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);   // For group_id filter
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MemberDTO memberDTO = new MemberDTO();
                    memberDTO.setId(rs.getInt("account_id"));
                    memberDTO.setName(rs.getString("username"));
                    memberDTO.setAvatar(rs.getString("avatar"));
                    memberDTO.setDate(rs.getTimestamp("participate_start_date").toLocalDateTime());
                    memberList.add(memberDTO);
                }
            }
            logger.info("Successfully retrieved " + memberList.size() + " members for group ID: " + groupId);
        } catch (SQLException e) {
            logger.severe("Failed to retrieve members for group ID: " + groupId + " - Error: " + e.getMessage());
        }

        return memberList;
    }

    public List<MemberDTO> getManagers(int accountId, int groupId) {
        logger.info("Retrieving managers for group ID: " + groupId + " for account ID: " + accountId);
        List<MemberDTO> managerList = new ArrayList<>();
        String sql = """
                SELECT a.account_id, a.username, a.avatar, m.manage_start_date \
                FROM manage m \
                JOIN account a ON m.account_id = a.account_id \
                WHERE m.group_id = ? AND a.account_status = 'active'""";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);   // For group_id filter
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MemberDTO memberDTO = new MemberDTO();
                    memberDTO.setId(rs.getInt("account_id"));
                    memberDTO.setName(rs.getString("username"));
                    memberDTO.setAvatar(rs.getString("avatar"));
                    memberDTO.setDate(rs.getTimestamp("manage_start_date").toLocalDateTime());
                    managerList.add(memberDTO);
                }
            }
            logger.info("Successfully retrieved " + managerList.size() + " managers for group ID: " + groupId);
        } catch (SQLException e) {
            logger.severe("Failed to retrieve managers for group ID: " + groupId + " - Error: " + e.getMessage());
        }

        return managerList;
    }

    public boolean leaveGroup(int accountId, int groupId) {
        logger.info("Attempting to remove account ID: " + accountId + " from group ID: " + groupId);
        boolean success = false;

        try (Connection conn = new DBContext().getConnection()) {
            // Delete from participate table
            String participateSql = "DELETE FROM participate WHERE account_id = ? AND group_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(participateSql)) {
                stmt.setInt(1, accountId);
                stmt.setInt(2, groupId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    logger.info("Successfully removed account ID: " + accountId + " from participate table for group ID: " + groupId);
                    success = true;
                }
            }

            // Delete from manage table
            String manageSql = "DELETE FROM manage WHERE account_id = ? AND group_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(manageSql)) {
                stmt.setInt(1, accountId);
                stmt.setInt(2, groupId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    logger.info("Successfully removed account ID: " + accountId + " from manage table for group ID: " + groupId);
                    success = true;
                }
            }

            if (!success) {
                logger.warning("Account ID: " + accountId + " was not found in participate or manage tables for group ID: " + groupId);
            }
        } catch (SQLException e) {
            logger.severe("Failed to remove account ID: " + accountId + " from group ID: " + groupId + " - Error: " + e.getMessage());
            return false;
        }

        return success;
    }

    public boolean feedback(int accountId, int groupId, String content) {
        logger.info("Attempting to insert feedback for account ID: " + accountId + " and group ID: " + groupId);

        String sql = "INSERT INTO feedback_group (account_id, group_id, feedback_group_content) " +
                     "VALUES (?, ?, ?)";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            stmt.setString(3, content);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warning("Failed to insert feedback for account ID: " + accountId + " and group ID: " + groupId);
                return false;
            }
            logger.info("Successfully inserted feedback for account ID: " + accountId + " and group ID: " + groupId);
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to insert feedback for account ID: " + accountId + " and group ID: " + groupId + " - Error: " + e.getMessage());
            return false;
        }
    }

    public boolean isManager(int accountId, int groupId) {
        logger.info("Checking if account ID: " + accountId + " is a manager for group ID: " + groupId);
        String sql = "SELECT 1 FROM manage WHERE account_id = ? AND group_id = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("Account ID: " + accountId + " is a manager for group ID: " + groupId);
                    return true;
                } else {
                    logger.info("Account ID: " + accountId + " is not a manager for group ID: " + groupId);
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to check manager status for account ID: " + accountId + " and group ID: " + groupId + " - Error: " + e.getMessage());
            return false;
        }
    }

    public boolean isMember(int accountId, int groupId) {
        logger.info("Checking if account ID: " + accountId + " is a manager for group ID: " + groupId);
        String sql = "SELECT 1 FROM participate WHERE account_id = ? AND group_id = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("Account ID: " + accountId + " is a manager for group ID: " + groupId);
                    return true;
                } else {
                    logger.info("Account ID: " + accountId + " is not a manager for group ID: " + groupId);
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to check manager status for account ID: " + accountId + " and group ID: " + groupId + " - Error: " + e.getMessage());
            return false;
        }
    }

    public boolean isLeader(int accountId, int groupId) {
        logger.info("Checking if account ID: " + accountId + " is the leader for group ID: " + groupId);
        String sql = "SELECT 1 FROM [group] WHERE account_id = ? AND group_id = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("Account ID: " + accountId + " is the leader for group ID: " + groupId);
                    return true;
                } else {
                    logger.info("Account ID: " + accountId + " is not the leader for group ID: " + groupId);
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to check leader status for account ID: " + accountId + " and group ID: " + groupId + " - Error: " + e.getMessage());
            return false;
        }
    }

    public Group getGroupProfile(int groupId) {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("Failed to get database connection");
                return null;
            }

            String sql = """
                    SELECT * FROM "group" WHERE group_id = ? AND group_status = 'active'""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Group group = new Group();
                group.setId(rs.getInt("group_id"));
                group.setGroupName(rs.getString("group_name"));
                group.setGroupCoverImage(rs.getString("group_cover_image"));
                group.setGroupDescription(rs.getString("group_description"));
                return group;
            }
            return null;
        } catch (Exception e) {
            logger.warning("Failed to get group profile for group ID: " + groupId + " - Error: " + e.getMessage());
            return null;
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

    public boolean joinGroup(int accountId, String content, int groupId) {

        logger.info("Attempting to create join request for account ID: " + accountId + " and group ID: " + groupId);

        String sql = "INSERT INTO join_group_request (account_id, group_id, join_group_request_content, join_group_request_status) " +
                     "VALUES (?, ?, ?, 'sent')";

        try (Connection conn = new DBContext().getConnection();
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

    /*=== Group manager section ===*/

    public ArrayList<FeedbackGroupDTO> getAllFeedback(int groupId) {
        Connection conn;
        ArrayList<FeedbackGroupDTO> feedbackGroups = new ArrayList<>();
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("Failed to get database connection");
                return feedbackGroups;
            }

            String sql = """
                    SELECT fg.*, a.* FROM feedback_group fg \
                    JOIN account a ON a.account_id = fg.account_id \
                    WHERE group_id = ?""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                FeedbackGroupDTO feedbackGroup = new FeedbackGroupDTO();
                feedbackGroup.setContent(rs.getString("feedback_group_content"));
                feedbackGroup.setRequesterID(rs.getInt("account_id"));
                feedbackGroup.setUsername(rs.getString("username"));
                feedbackGroup.setAvatar(rs.getString("avatar"));
                feedbackGroups.add(feedbackGroup);
            }
        } catch (Exception e) {
            logger.warning("Failed to get feedback");
        }

        return feedbackGroups;
    }

    public boolean setGroupProfile(Group group) {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("Failed to get database connection");
                return false;
            }
            conn.setAutoCommit(false);

            String sql = """
                    UPDATE "group" \
                    SET group_name = ?, group_description = ?, group_cover_image = ? \
                    WHERE group_id = ?""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, group.getGroupName());
            stmt.setString(2, group.getGroupDescription());
            stmt.setString(3, group.getGroupCoverImage());
            stmt.setInt(4, group.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("Failed to set group profile");
                conn.rollback();
                return false;
            }

            logger.info("Set group profile successful");
            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning("Failed to set group profile for group ID: " + group.getId() + " - Error: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<JoinGroupRequestDTO> getAllPendingUser(int groupID) {
        Connection conn;
        ArrayList<JoinGroupRequestDTO> pendingUsers = new ArrayList<>();
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("Failed to get database connection");
                return pendingUsers;
            }

            String sql = """
                    SELECT *, jgr.* FROM account a \
                    JOIN join_group_request jgr ON a.account_id = jgr.account_id \
                    WHERE group_id = ? AND join_group_request_status = 'sent'""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JoinGroupRequestDTO account = new JoinGroupRequestDTO();
                account.setRequesterID(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setFullname(rs.getString("fullname"));
                account.setCreatedAt(rs.getTimestamp("join_group_request_date").toLocalDateTime());
                account.setAvatar(rs.getString("avatar"));
                account.setMessage(rs.getString("join_group_request_content"));
                pendingUsers.add(account);
            }
        } catch (Exception e) {
            logger.warning("Failed to get pending users " + e.getMessage());
        }
        return pendingUsers;
    }

    public boolean setPendingUsers(int userID, int groupID, boolean isAccepting) {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("Failed to get database connection");
                return false;
            }
            conn.setAutoCommit(false);

            if (!isAccepting) {
                String UpdateJGRsql = """
                    UPDATE join_group_request
                    SET join_group_request_status = 'rejected'
                    WHERE account_id = ? AND group_id = ?""";
                PreparedStatement updateStmt = conn.prepareStatement(UpdateJGRsql);
                updateStmt.setInt(1, userID);
                updateStmt.setInt(2, groupID);
                int affectedRows = updateStmt.executeUpdate();
                if (affectedRows == 0) {
                    logger.warning("Failed to reject join request");
                    conn.rollback();
                    return false;
                }
                return true;
            }

            String UpdateJGRsql = """
                    UPDATE join_group_request
                    SET join_group_request_status = 'accepted'
                    WHERE account_id = ? AND group_id = ?""";
            PreparedStatement updateStmt = conn.prepareStatement(UpdateJGRsql);
            updateStmt.setInt(1, userID);
            updateStmt.setInt(2, groupID);
            int affectedRows = updateStmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("Failed to set group pending users status");
                conn.rollback();
                return false;
            }

            String sql = """
                    INSERT INTO participate VALUES (?, ?, ?)""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            stmt.setInt(2, groupID);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("Failed to add to participate table");
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            logger.warning("Failed to process pending users");
            return false;
        }
    }

    public boolean kickMember(int accountId, int groupId) {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("Failed to get database connection");
                return false;
            }
            conn.setAutoCommit(false);

            String sql = """
                    DELETE FROM participate WHERE account_id = ? AND group_id = ?""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("Failed to kick member");
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning("Failed to kick member " + e.getMessage());
            return false;
        }
    }

    public boolean assignManager(int accountId, int groupId) {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("Failed to get database connection");
                return false;
            }
            conn.setAutoCommit(false);

            String sql = """
                    INSERT INTO manage VALUES (?, ?, ?)""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, accountId);
            stmt.setInt(2, groupId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("Failed to assign manager");
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning("Failed to assign manager " + e.getMessage());
            return false;
        }
    }

    public boolean processPost(int postID, boolean isApprove) {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("Failed to get database connection");
                return false;
            }
            conn.setAutoCommit(false);

            String sql = """
                    UPDATE post SET post_status = ? WHERE post_id = ?""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, isApprove ? "published" : "rejected");
            stmt.setInt(2, postID);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("Failed to process post");
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            logger.warning("Failed to process post " + e.getMessage());
            return false;
        }
    }

    /*=== Admin section ===*/

    public List<ResGroupDTO> getActiveGroups() {
        String sql = """
                SELECT [group].*, number_of_participant, number_of_post, active_account.* FROM [group]
                LEFT JOIN
                (SELECT group_id, COUNT(*) AS number_of_participant FROM participate\s
                JOIN account ON account.account_id = participate.account_id
                WHERE account.account_status = 'active'
                GROUP BY participate.group_id) AS participant
                ON [group].group_id = participant.group_id
                LEFT JOIN
                (SELECT group_id, COUNT(*) AS number_of_post FROM post
                WHERE post.post_status = 'published'
                GROUP BY post.group_id) AS post
                ON post.group_id = [group].group_id
                LEFT JOIN manage ON [group].group_id = manage.group_id
                LEFT JOIN [active_account] ON manage.account_id = [active_account].account_id
                WHERE group_status = 'active'""";
        Map<Integer, ResGroupDTO> mapGroups = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int groupId = rs.getInt("group_id");
                ResGroupDTO group = mapGroups.get(groupId);
                if (group == null) {
                    group = new ResGroupDTO();
                    group.setId(groupId);
                    group.setName(rs.getString("group_name"));
                    group.setImage(rs.getString("group_cover_image"));
                    group.setDescription(rs.getString("group_description"));
                    group.setCreateDate(rs.getTimestamp("group_create_date") != null
                            ? rs.getTimestamp("group_create_date").toLocalDateTime() : null);
                    group.setStatus(rs.getString("group_status"));
                    group.setNumberParticipants(rs.getInt("number_of_participant"));
                    group.setNumberPosts(rs.getInt("number_of_post"));
                    mapGroups.put(groupId, group);
                }
                if (rs.getInt("account_id") != 0) {
                    Account account = new Account();
                    account.setId(rs.getInt("account_id"));
                    account.setUsername(rs.getString("username"));
                    account.setAvatar(rs.getString("avatar"));
                    account.setFullname(rs.getString("fullname"));
                    group.addManager(account);
                }

            }

            return new ArrayList<>(mapGroups.values());
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    public List<ResGroupDTO> getInactiveGroups() {
        String sql = """
                SELECT [group].*, [active_account].* FROM [group]
                JOIN [active_account] ON [group].account_id = [active_account].account_id
                WHERE group_status = 'inactive'""";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            List<ResGroupDTO> groups = new ArrayList<>();
            while (rs.next()) {
                ResGroupDTO group = new ResGroupDTO();
                group.setId(rs.getInt("group_id"));
                group.setName(rs.getString("group_name"));
                group.setImage(rs.getString("group_cover_image"));
                group.setDescription(rs.getString("group_description"));
                group.setCreateDate(rs.getTimestamp("group_create_date") != null
                        ? rs.getTimestamp("group_create_date").toLocalDateTime() : null);
                group.setStatus(rs.getString("group_status"));

                Account account = new Account();
                account.setId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setAvatar(rs.getString("avatar"));
                account.setFullname(rs.getString("fullname"));
                group.addManager(account);

                groups.add(group);
                }
            return groups;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
        return null;
    }

    public boolean banGroup(int groupId) {
        try {
            String sql = """
                    UPDATE [group]
                    SET group_status = 'banned'
                    WHERE group_id = ?""";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, groupId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 0) {
                return true;
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
        return false;
    }

    public boolean acceptGroup(int groupId) {
        try {
            String sql = """
                    UPDATE [group]
                    SET group_status = 'active'
                    WHERE group_id = ?""";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, groupId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 0) {
                return true;
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
        return false;
    }

    public boolean rejectGroup(int groupId) {
        try {
            String sql = """
                    UPDATE [group]
                    SET group_status = 'rejected'
                    WHERE group_id = ?""";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, groupId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 0) {
                return true;
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
        return false;
    }

    public ArrayList<GroupReport> getReports(int groupId, int userID) {
        Connection conn;
        ArrayList<GroupReport> reports = new ArrayList<>();
        try {
            conn = (new DBContext()).getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return reports;
            }

            /*=== Fetch reported posts ===*/

            //Get all posts in the group
            PostDAO postDAO = new PostDAO();
            ArrayList<RespPostDTO> posts = postDAO.getPostsInGroup(userID, groupId);

            //Get all the reported post in database
            String postSQL = """
                    SELECT rp.*, a.* FROM report_post rp \
                    JOIN account a ON a.account_id = rp.account_id \
                    WHERE rp.report_status = 'sent'""";
            PreparedStatement postStmt = conn.prepareStatement(postSQL);
            ResultSet rs = postStmt.executeQuery();
            while (rs.next()) {
                //Get postID from list of report
                int reportedPostID = rs.getInt("post_id");
                //Check if this post is posted within group
                RespPostDTO reportedPost = posts.stream()
                        .filter(post -> post.getPostId() == reportedPostID)
                        .findFirst()
                        .orElse(null);
                if (reportedPost != null) {
                    GroupReport report = new GroupReport();
                    report.setReportedPost(reportedPost);
                    report.setReportID(rs.getInt("report_id"));
                    report.setReportContent(rs.getString("report_content"));
                    report.setReporterID(rs.getInt("account_id"));
                    report.setReporterUsername(rs.getString("username"));
                    report.setReporterAvatar(rs.getString("avatar"));

                    System.out.println(report.getReportedPost());
                    reports.add(report);
                    logger.info(reports.size() + " reports have been reported 1");
                }
            }

            /*=== Fetch reported comments ===*/

            //Get all comment belongs to a group in database
            String cmtSQL = """
                    SELECT rc.*, p.*, a.*, c.* FROM report_comment rc \
                    JOIN comment c ON c.comment_id = rc.comment_id \
                    JOIN post p ON p.post_id = c.post_id \
                    JOIN account a ON a.account_id = rc.account_id \
                    WHERE p.group_id = ? AND rc.report_status = 'sent'""";
            PreparedStatement cmtStmt = conn.prepareStatement(cmtSQL);
            cmtStmt.setInt(1, groupId);
            ResultSet cmtRs = cmtStmt.executeQuery();
            while (cmtRs.next()) {
                //Create RespCommentDTO
                RespCommentDTO comment = new RespCommentDTO();
                comment.setId(cmtRs.getInt("comment_id"));
                comment.setContent(cmtRs.getString("comment_content"));
                comment.setImage(cmtRs.getString("comment_image"));
                comment.setCreatedAt(cmtRs.getTimestamp("comment_create_date") != null ?
                                cmtRs.getTimestamp("comment_create_date").toLocalDateTime() :
                                null);
                comment.setUpdatedAt(cmtRs.getTimestamp("comment_last_update") != null ?
                        cmtRs.getTimestamp("comment_last_update").toLocalDateTime() : null);
                comment.setTotalLikes(0);
                comment.setAccountID(cmtRs.getInt("account_id"));
                comment.setUsername(cmtRs.getString("username"));
                comment.setAvatar(cmtRs.getString("avatar"));
                comment.setPostID(cmtRs.getInt("post_id"));
                comment.setReplyID(cmtRs.getInt("reply_comment_id"));
                comment.setOwnComment(false);
                comment.setLiked(false);

                //Create GroupReport
                GroupReport report = new GroupReport();
                report.setReportID(cmtRs.getInt("report_id"));
                report.setReportContent(cmtRs.getString("report_content"));
                report.setReporterID(cmtRs.getInt("account_id"));
                report.setReporterUsername(cmtRs.getString("username"));
                report.setReporterAvatar(cmtRs.getString("avatar"));
                report.setReportedComment(comment);
                reports.add(report);
                logger.info(reports.size() + " reports have been reported 2");
            }
        } catch (Exception e) {
            logger.warning("Failed to get reports in group " + e.getMessage());
            logger.warning(e.getStackTrace().toString());
            return reports;
        }
        return reports;
    }

    public boolean processReport(int reportId, boolean isApprove, String reportType) {
        Connection conn;
        try {
            conn = (new DBContext()).getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return false;
            }
            conn.setAutoCommit(false);

            String tableName;
            if ("post".equalsIgnoreCase(reportType)) {
                tableName = "report_post";
            } else if ("comment".equalsIgnoreCase(reportType)) {
                tableName = "report_comment";
            } else {
                logger.warning("Invalid report type provided: " + reportType);
                return false;
            }

            String sql = "UPDATE " + tableName + " SET report_status = ? WHERE report_id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, isApprove ? "accepted" : "rejected");
            stmt.setInt(2, reportId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                logger.info("Report " + reportId + " in " + tableName + " processed successfully with action: " + (isApprove ? "accepted" : "rejected"));
                return true;
            }

            conn.rollback();
            logger.warning("Report processing failed! No rows affected for report_id: " + reportId + " in table: " + tableName);
            return false;
        } catch (Exception e) {
            logger.warning("Failed to process group report " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        GroupDAO dao = new GroupDAO();
        System.out.println(dao.getReports(3, 1).size());
        System.out.println();
    }
}
