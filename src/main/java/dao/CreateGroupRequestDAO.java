package dao;

import dto.ReqGroupDTO;
import dto.ResCreateGroupRequestDTO;
import model.Group;
import util.database.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CreateGroupRequestDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public boolean sendCreateGroupRequest(String content, int accountId) {
        Connection conn;
        try {
            conn = getConnection();
            if (conn == null) {
                return false;
            }

            String sql = "INSERT INTO create_group_request(create_group_request_content, account_id) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, content);
            stmt.setInt(2, accountId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public List<ResCreateGroupRequestDTO> getSendedCreateGroupRequests() {
        String sql = "SELECT * FROM create_group_request\n" +
                "JOIN account ON account.account_id = create_group_request.account_id\n" +
                "WHERE create_group_request_status = 'sended'\n" +
                "ORDER BY create_group_request_date DESC";
        List<ResCreateGroupRequestDTO> requests = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("create_group_request_id");
                String content = rs.getString("create_group_request_content");
                LocalDateTime date = rs.getTimestamp("create_group_request_date").toLocalDateTime();
                String status = rs.getString("create_group_request_status");
                String accountName = rs.getString("fullname");
                String accountAvatar = rs.getString("avatar");

                ResCreateGroupRequestDTO request = new ResCreateGroupRequestDTO(id, content, date, status, accountAvatar, accountName);
                requests.add(request);
            }

            return requests;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    public boolean rejectCreateGroupRequest(int id) {
        try {
            String sql = "UPDATE create_group_request\n" +
                    "SET create_group_request_status = 'rejected'\n" +
                    "WHERE create_group_request_id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, id);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows != 0) {
                    return true;
                }
        } catch (SQLException e) {
                logger.warning(e.getMessage());
        }
        return false;
    }

    public int getSenderId(int id) {
        String sql = "SELECT * FROM create_group_request\n" +
                "WHERE create_group_request_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int senderId = rs.getInt("account_id");
                return senderId;
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
        return 0;
    }

    public boolean approveCreateGroupRequest(int id, int senderId) {
        Connection conn;
        try {
            conn = getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);

            String sql = "UPDATE create_group_request\n" +
                    "SET create_group_request_status = 'accepted'\n" +
                    "WHERE create_group_request_id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            String groupSql = "INSERT INTO [group](group_name) VALUES\n" +
                            "(?)";
            PreparedStatement groupSt = conn.prepareStatement(groupSql, PreparedStatement.RETURN_GENERATED_KEYS);
            groupSt.setString(1, "Approved Group ID" + id);
            affectedRows = groupSt.executeUpdate();
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

            String manageSql = "INSERT INTO manage(group_id, account_id) VALUES(?, ?)";
            PreparedStatement manageSt = conn.prepareStatement(manageSql);
            manageSt.setInt(1, groupId);
            manageSt.setInt(2, senderId);
            manageSt.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        CreateGroupRequestDAO dao = new CreateGroupRequestDAO();
//        System.out.println(dao.sendCreateGroupRequest("<UNK>", 1));
//        System.out.println(dao.getCreateGroupRequests().get(0));
//        System.out.println(dao.rejectCreateGroupRequest(3));
//        System.out.println(dao.approveCreateGroupRequest(3, 1));
    }

}
