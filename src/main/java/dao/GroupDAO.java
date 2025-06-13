// Khai báo package
package dao;

// Import các lớp cần thiết
import dto.GroupProfileDTO;
import util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Lớp DAO dùng để thao tác với bảng "group" trong database
public class GroupDAO {
    private final Connection connection;

    // Constructor: tạo kết nối đến database khi khởi tạo GroupDAO
    public GroupDAO() {
        this.connection = new DBContext().getConnection(); // Lấy connection từ DBContext (class tự định nghĩa để quản lý kết nối)

        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }

        // Kiểm tra kết nối có hợp lệ không (dùng để debug)
        try {
            if (!connection.isValid(1)) {
                System.out.println("Debug: Database connection is not valid");
            } else {
                System.out.println("Debug: Database connection is valid");
            }
        } catch (SQLException e) {
            System.out.println("Debug: Failed to validate connection: " + e.getMessage());
        }
    }

    // Hàm lấy thông tin group profile dựa theo groupId và accountId = Group manager hoặc admin
    public GroupProfileDTO getGroupProfile(int groupId) {
        String sql = "SELECT group_id, group_name, group_description, group_cover_image, group_create_date, group_status " +
                "FROM [group] " +
                "WHERE group_id = ? ";
//        AND (created_by = ? OR ? IN (SELECT account_id FROM group_member WHERE group_id = ? AND is_admin = 1))";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Gán giá trị vào các dấu ?
            stmt.setInt(1, groupId);
//            stmt.setInt(2, accountId);
//            stmt.setInt(3, accountId);
//            stmt.setInt(4, groupId);

            // Thực thi câu lệnh và xử lý kết quả
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Nếu có dữ liệu, tạo DTO và gán giá trị
                    GroupProfileDTO dto = new GroupProfileDTO();
                    dto.setGroupId(rs.getInt("group_id"));
                    dto.setGroupName(rs.getString("group_name"));
                    dto.setDescription(rs.getString("group_description"));
                    dto.setAvatarPath(rs.getString("group_cover_image"));
                    dto.setStatus(rs.getString("group_status"));
//                    dto.setCreatedBy(rs.getInt("created_by"));
                    dto.setGroupCreateDate(rs.getTimestamp("group_create_date").toLocalDateTime());

                    // last_updated có thể null
//                    dto.setLastUpdated(rs.getTimestamp("last_updated") != null
//                            ? rs.getTimestamp("last_updated").toLocalDateTime()
//                            : null);

                    return dto;
                }
            }
        } catch (SQLException e) {
            // Nếu có lỗi, in ra lỗi để debug
            System.err.println("SQL error in getGroupProfile: " + e.getMessage());
            e.printStackTrace();
        }

        // Nếu không có kết quả, trả về null
        return null;
    }

    // Hàm cập nhật thông tin profile của group (chỉ cho người tạo group)
    public boolean updateGroupProfile(GroupProfileDTO dto) {
        String sql = "UPDATE [group] SET group_name = ?, group_description = ?, group_cover_image = ?, group_status = ? " +//group_create_date = ?, last_updated = CURRENT_TIMESTAMP " +
                "WHERE group_id = ? ";


        try (
                // Sử dụng connection hiện tại và chuẩn bị statement
                Connection conn = connection;
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            // Gán giá trị vào các tham số ?
            stmt.setString(1, dto.getGroupName());
            stmt.setString(2, dto.getDescription());
            stmt.setString(3, dto.getAvatarPath());
            stmt.setString(4, dto.getStatus());
//            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, dto.getGroupId());
//            stmt.setInt(7, accountId);

            // Thực thi update
            int rowsAffected = stmt.executeUpdate();

            // Trả về true nếu có ít nhất 1 dòng bị ảnh hưởng (tức là cập nhật thành công)
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Bắt và in lỗi nếu có
            System.err.println("SQL error in updateGroupProfile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
