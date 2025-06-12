package dao;

import dto.GroupProfileDTO;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class GroupDAO {
    private final Connection connection;

    public GroupDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }
    }

    public GroupProfileDTO getGroupProfile(int groupId, int accountId) {
        String sql = "SELECT group_id, group_name, group_description, avatar_path, status, created_by, group_create_date, last_updated " +
                "FROM [group] WHERE group_id = ? AND (created_by = ? OR ? IN (SELECT account_id FROM group_member WHERE group_id = ? AND is_admin = 1))";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, accountId);
            stmt.setInt(3, accountId);
            stmt.setInt(4, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    GroupProfileDTO dto = new GroupProfileDTO();
                    dto.setGroupId(rs.getInt("group_id"));
                    dto.setGroupName(rs.getString("group_name"));
                    dto.setDescription(rs.getString("group_description"));
                    dto.setAvatarPath(rs.getString("avatar_path"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatedBy(rs.getInt("created_by"));
                    dto.setGroupCreateDate(rs.getTimestamp("group_create_date").toLocalDateTime());
                    dto.setLastUpdated(rs.getTimestamp("last_updated") != null ? rs.getTimestamp("last_updated").toLocalDateTime() : null);
                    return dto;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error in getGroupProfile: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateGroupProfile(GroupProfileDTO dto, int accountId) {
        String sql = "UPDATE [group] SET group_name = ?, group_description = ?, avatar_path = ?, status = ?, last_updated = ? " +
                "WHERE group_id = ? AND created_by = ?";
        try (Connection conn = connection; PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dto.getGroupName());
            stmt.setString(2, dto.getDescription());
            stmt.setString(3, dto.getAvatarPath());
            stmt.setString(4, dto.getStatus());
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(6, dto.getGroupId());
            stmt.setInt(7, accountId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL error in updateGroupProfile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}