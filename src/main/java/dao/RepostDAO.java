package dao;

import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RepostDAO {
    private final Connection connection;

    public RepostDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }
    }

    public boolean isPostReposted(int accountId, int postId) {
        String sql = "SELECT COUNT(*) FROM repost WHERE account_id = ? AND post_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error in isPostReposted: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean repost(int accountId, int postId) {
        String sql = "INSERT INTO repost (account_id, post_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, postId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL error in repost: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean unrepost(int accountId, int postId) {
        String sql = "DELETE FROM repost WHERE account_id = ? AND post_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, postId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                System.err.println("No rows affected when unreposting post " + postId);
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL error in unrepost: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int getRepostCount(int postId) {
        String sql = "SELECT COUNT(*) FROM repost WHERE post_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error in getRepostCount: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}