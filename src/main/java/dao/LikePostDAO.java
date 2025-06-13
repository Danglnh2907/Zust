package dao;

import model.Account;
import model.Post;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class LikePostDAO {
    private final Connection connection;

    public LikePostDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }
    }

    public boolean isPostLiked(int accountId, int postId) {
        String sql = "SELECT COUNT(*) FROM like_post WHERE account_id = ? AND post_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error in isPostLiked: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean likePost(int accountId, int postId) {
        String sql = "INSERT INTO like_post (account_id, post_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, postId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL error in likePost: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean unlikePost(int accountId, int postId) {
        String sql = "DELETE FROM like_post WHERE account_id = ? AND post_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, postId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                System.err.println("No rows affected when unliking post " + postId);
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL error in unlikePost: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int getLikeCount(int postId) {
        String sql = "SELECT COUNT(*) FROM like_post WHERE post_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error in getLikeCount: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}