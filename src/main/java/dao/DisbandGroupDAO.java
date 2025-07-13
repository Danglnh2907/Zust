package dao;

import util.database.DBContext;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DisbandGroupDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public boolean disbandGroup(int groupId) {
        try {
            String sql = "UPDATE [group] SET group_status = 'deleted' WHERE group_id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, groupId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }
}