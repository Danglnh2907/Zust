package dao;

import util.database.DBContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import model.Group;
import dto.ReqGroupDTO;
import dto.ResGroupDTO;


public class GroupDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public boolean createGroup(ReqGroupDTO group) {
        Connection conn;
        try {
            //Get connection
            conn = getConnection();
            if (conn == null) {
                return false;
            }
            //Start transaction
            conn.setAutoCommit(false);

            //Insert into post table
            String postSql = "INSERT INTO [group](group_name, group_description, group_cover_image) VALUES\n" +
                             "(?, ?, ?)";
            PreparedStatement groupSt = conn.prepareStatement(postSql, PreparedStatement.RETURN_GENERATED_KEYS);
            groupSt.setString(1, group.getGroupName());
            groupSt.setString(2, group.getGroupDescription());
            groupSt.setString(3, group.getCoverImage());
            int affectedRows = groupSt.executeUpdate();
            if (affectedRows == 0) {
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
        String sql = "SELECT [group].*, number_of_participant, number_of_post FROM [group]\n" +
                "LEFT JOIN \n" +
                "(SELECT group_id, COUNT(*) AS number_of_participant FROM participate \n" +
                "GROUP BY participate.group_id) AS participant\n" +
                "ON [group].group_id = participant.group_id\n" +
                "LEFT JOIN\n" +
                "(SELECT group_id, COUNT(*) AS number_of_post FROM post \n" +
                "GROUP BY post.group_id) AS post\n" +
                "ON post.group_id = [group].group_id\n" +
                "WHERE [group].group_id = ?\n";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ResGroupDTO group = new ResGroupDTO();
                group.setId(rs.getInt("group_id"));
                group.setName(rs.getString("group_name"));
                group.setImage(rs.getString("group_cover_image"));
                group.setDescription(rs.getString("group_description"));
                group.setCreate_date(rs.getTimestamp("group_create_date") != null
                        ? rs.getTimestamp("group_create_date").toLocalDateTime() : null);
                group.setNumberParticipants(rs.getInt("number_of_participant"));
                group.setNumberPosts(rs.getInt("number_of_post"));
                return group;
            }

            return null;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        GroupDAO dao = new GroupDAO();
        System.out.println(dao.getGroup(1));
    }

}
