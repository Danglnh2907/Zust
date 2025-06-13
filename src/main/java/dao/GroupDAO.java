package dao;

import model.Account;
import util.database.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import dto.ReqGroupDTO;
import dto.ResGroupDTO;


public class GroupDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public boolean createGroup(ReqGroupDTO group) {
        Connection conn;
        try {
            conn = getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);
            String groupSql = "INSERT INTO [group](group_name, group_description, group_cover_image) VALUES\n" +
                             "(?, ?, ?)";
            PreparedStatement groupSt = conn.prepareStatement(groupSql, PreparedStatement.RETURN_GENERATED_KEYS);
            groupSt.setString(1, group.getGroupName());
            groupSt.setString(2, group.getGroupDescription());
            groupSt.setString(3, group.getCoverImage());
            int affectedRows = groupSt.executeUpdate();
            if (affectedRows == 0) {
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

            List<Integer> managers = group.getManagers();

            String participateSql = "INSERT INTO participate(group_id, account_id) VALUES(?, ?)";
            PreparedStatement participateSt = conn.prepareStatement(participateSql);
            for (Integer manager : managers) {
                participateSt.setInt(1, groupId);
                participateSt.setInt(2, manager);
                participateSt.addBatch();
            }

            int[] listAffectedRows = participateSt.executeBatch();
            for (int affectedRow : listAffectedRows) {
                if (affectedRow == 0) {
                    conn.rollback();
                    return false;
                }
            }

            String manageSql = "INSERT INTO manage(group_id, account_id) VALUES(?, ?)";
            PreparedStatement manageSt = conn.prepareStatement(manageSql);
            for (Integer manager : managers) {
                manageSt.setInt(1, groupId);
                manageSt.setInt(2, manager);
                manageSt.addBatch();
            }
            listAffectedRows = manageSt.executeBatch();
            for (int listAffectedRow : listAffectedRows) {
                if (listAffectedRow == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public ResGroupDTO getActiveGroup(int groupId) {
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
                "WHERE [group].group_id = ? AND group_status = 'active' AND account.account_status = 'active'";

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
                    Account account = new Account();
                    account.setId(rs.getInt("account_id"));
                    account.setUsername(rs.getString("username"));
                    account.setAvatar(rs.getString("avatar"));
                    account.setFullname(rs.getString("fullname"));
                    group.addManager(account);
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
                "WHERE group_status = 'active' AND account.account_status = 'active'";
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
                Account account = new Account();
                account.setId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setAvatar(rs.getString("avatar"));
                account.setFullname(rs.getString("fullname"));
                group.addManager(account);
            }

            List<ResGroupDTO> groups = new ArrayList<>();
            groups.addAll(mapGroups.values());
            return groups;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    public boolean disbandGroup(int groupId) {
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

    public static void main(String[] args) {
        GroupDAO dao = new GroupDAO();
        ReqGroupDTO dto = new ReqGroupDTO("Test", "Test", "image");
        dto.addManager(1);
        System.out.println(dao.createGroup(dto));
//        System.out.println(dao.getGroupMembers(31));
//        System.out.println(dao.getActiveGroup(31).getManagers());
//        List<ResGroupDTO> groups = dao.getActiveGroups();
//        System.out.println(groups);
//        System.out.println(dao.assignManager(31, new int[] {1}));
//        System.out.println(dao.deleteManager(31, 3));
    }


}
