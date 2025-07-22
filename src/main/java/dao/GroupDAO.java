package dao;

import model.InteractGroupDTO;
import model.MemberDTO;
import model.Account;
import util.database.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import model.ReqGroupDTO;
import model.ResGroupDTO;


public class GroupDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

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

    public ResGroupDTO getGroup(int groupId) {
        String sql = "SELECT [group].*, number_of_participant, number_of_post, account.* FROM [group]\n" +
                "LEFT JOIN \n" +
                "(SELECT group_id, COUNT(*) AS number_of_participant FROM participate \n" +
                "JOIN account ON account.account_id = participate.account_id\n" +
                "WHERE account.account_status = 'active'\n" +
                "GROUP BY participate.group_id) AS participant\n" +
                "ON [group].group_id = participant.group_id\n" +
                "LEFT JOIN\n" +
                "(SELECT group_id, COUNT(*) AS number_of_post FROM post\n" +
                "WHERE post.post_status = 'publish'\n" +
                "GROUP BY post.group_id) AS post\n" +
                "ON post.group_id = [group].group_id\n" +
                "LEFT JOIN manage ON [group].group_id = manage.group_id\n" +
                "LEFT JOIN account ON manage.account_id = account.account_id\n" +
                "WHERE [group].group_id = ? AND NOT group_status = 'deleted'";

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

    public List<ResGroupDTO> getActiveGroups() {
        String sql = "SELECT [group].*, number_of_participant, number_of_post, active_account.* FROM [group]\n" +
                "LEFT JOIN \n" +
                "(SELECT group_id, COUNT(*) AS number_of_participant FROM participate \n" +
                "JOIN account ON account.account_id = participate.account_id\n" +
                "WHERE account.account_status = 'active'\n" +
                "GROUP BY participate.group_id) AS participant\n" +
                "ON [group].group_id = participant.group_id\n" +
                "LEFT JOIN\n" +
                "(SELECT group_id, COUNT(*) AS number_of_post FROM post\n" +
                "WHERE post.post_status = 'published'\n" +
                "GROUP BY post.group_id) AS post\n" +
                "ON post.group_id = [group].group_id\n" +
                "LEFT JOIN manage ON [group].group_id = manage.group_id\n" +
                "LEFT JOIN [active_account] ON manage.account_id = [active_account].account_id\n" +
                "WHERE group_status = 'active'";
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

            List<ResGroupDTO> groups = new ArrayList<>();
            groups.addAll(mapGroups.values());
            return groups;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    public List<ResGroupDTO> getInactiveGroups() {
        String sql = "SELECT [group].*, [active_account].* FROM [group]\n" +
                "JOIN [active_account] ON [group].account_id = [active_account].account_id\n" +
                "WHERE group_status = 'inactive'";
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
            String sql = "UPDATE [group]\n" +
                    "SET group_status = 'banned'\n" +
                    "WHERE group_id = ?";
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
            String sql = "UPDATE [group]\n" +
                    "SET group_status = 'active'\n" +
                    "WHERE group_id = ?";
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
            String sql = "UPDATE [group]\n" +
                    "SET group_status = 'rejected'\n" +
                    "WHERE group_id = ?";
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

    public List<Account> getGroupMembers(int groupId) {
        String sql = "SELECT * FROM participate\n" +
                "JOIN account ON participate.account_id = account.account_id\n" +
                "WHERE group_id = ? AND account_status = 'active' \n" +
                "AND participate.account_id NOT IN \n" +
                "(SELECT account_id FROM manage WHERE group_id = ?)";
        Map<Integer, ResGroupDTO> mapGroups = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, groupId);
            ResultSet rs = stmt.executeQuery();

            List<Account> members = new ArrayList<>();
            while (rs.next()) {
                Account account = new Account();
                account.setId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setAvatar(rs.getString("avatar"));
                account.setFullname(rs.getString("fullname"));
                members.add(account);
            }
            return members;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    public boolean assignManager(int groupId, int managerId[]) {

        String manageSql = "INSERT INTO manage(group_id, account_id) VALUES(?, ?)";
        try(PreparedStatement manageSt = connection.prepareStatement(manageSql)){
            connection.setAutoCommit(false);
            for (int manager : managerId) {
                manageSt.setInt(1, groupId);
                manageSt.setInt(2, manager);
                manageSt.addBatch();
            }
            int[] listAffectedRows = manageSt.executeBatch();
            for (int listAffectedRow : listAffectedRows) {
                if (listAffectedRow == 0) {
                    connection.rollback();
                    return false;
                }
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public List<Account> getActiveAccountsManagers(int groupId) {
        List<Account> accounts = new ArrayList<Account>();
        String sql = "SELECT * FROM account\n" +
                "JOIN manage ON account.account_id = manage.account_id\n" +
                "WHERE account.account_role = 'user' AND account.account_status = 'active' AND manage.group_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Account account = new Account();
                account.setId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setFullname(rs.getString("fullname"));
                account.setAvatar(rs.getString("avatar"));
                account.setPhone(rs.getString("phone"));
                account.setEmail(rs.getString("email"));
                account.setDob(rs.getTimestamp("dob") != null
                        ? rs.getTimestamp("dob").toLocalDateTime().toLocalDate() : null);
                accounts.add(account);
            }
            return accounts;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }

    }

    public boolean deleteManager(int groupId, int managerId) {

        String manageSql = "DELETE FROM manage\n" +
                "WHERE group_id = ? AND account_id = ?";
        try(PreparedStatement manageSt = connection.prepareStatement(manageSql)){
            manageSt.setInt(1, groupId);
            manageSt.setInt(2, managerId);

            if(manageSt.executeUpdate() > 0){
                return true;
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
        return false;
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

    public List<MemberDTO> getMembers(int accountId, int groupId) {
        logger.info("Retrieving members for group ID: " + groupId + " for account ID: " + accountId);
        List<MemberDTO> memberList = new ArrayList<>();
        String sql = "SELECT a.account_id, a.username, a.avatar, p.participate_start_date, " +
                "CASE " +
                "    WHEN a.account_id = ? THEN 'SELF' " +
                "    WHEN EXISTS (SELECT 1 FROM interact i WHERE i.actor_account_id = ? AND i.target_account_id = a.account_id AND i.interact_status = 'friend') THEN 'FRIEND' " +
                "    WHEN EXISTS (SELECT 1 FROM interact i WHERE i.actor_account_id = ? AND i.target_account_id = a.account_id AND i.interact_status = 'block') THEN 'BLOCK' " +
                "    ELSE 'NORMAL' " +
                "END AS interact_status " +
                "FROM participate p " +
                "INNER JOIN account a ON p.account_id = a.account_id " +
                "WHERE p.group_id = ? AND a.account_status = 'active' " +
                "AND NOT EXISTS (SELECT 1 FROM manage m WHERE m.group_id = p.group_id AND m.account_id = p.account_id)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId); // For SELF check
            stmt.setInt(2, accountId); // For FRIEND check
            stmt.setInt(3, accountId); // For BLOCK check
            stmt.setInt(4, groupId);   // For group_id filter
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MemberDTO memberDTO = new MemberDTO();
                    memberDTO.setId(rs.getInt("account_id"));
                    memberDTO.setName(rs.getString("username"));
                    memberDTO.setAvatar(rs.getString("avatar"));
                    memberDTO.setInteractStatus(MemberDTO.InteractStatus.valueOf(rs.getString("interact_status")));
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
        String sql = "SELECT a.account_id, a.username, a.avatar, m.manage_start_date, " +
                "CASE " +
                "    WHEN a.account_id = ? THEN 'SELF' " +
                "    WHEN EXISTS (SELECT 1 FROM interact i WHERE i.actor_account_id = ? AND i.target_account_id = a.account_id AND i.interact_status = 'friend') THEN 'FRIEND' " +
                "    WHEN EXISTS (SELECT 1 FROM interact i WHERE i.actor_account_id = ? AND i.target_account_id = a.account_id AND i.interact_status = 'block') THEN 'BLOCK' " +
                "    ELSE 'NORMAL' " +
                "END AS interact_status " +
                "FROM manage m " +
                "INNER JOIN account a ON m.account_id = a.account_id " +
                "WHERE m.group_id = ? AND a.account_status = 'active'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId); // For SELF check
            stmt.setInt(2, accountId); // For FRIEND check
            stmt.setInt(3, accountId); // For BLOCK check
            stmt.setInt(4, groupId);   // For group_id filter
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MemberDTO memberDTO = new MemberDTO();
                    memberDTO.setId(rs.getInt("account_id"));
                    memberDTO.setName(rs.getString("username"));
                    memberDTO.setAvatar(rs.getString("avatar"));
                    memberDTO.setInteractStatus(MemberDTO.InteractStatus.valueOf(rs.getString("interact_status")));
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

    public static void main(String[] args) {
        GroupDAO dao = new GroupDAO();
        PostDAO postDao = new PostDAO();
        System.out.println(postDao.getPendingPosts(2, 1));
    }


}
