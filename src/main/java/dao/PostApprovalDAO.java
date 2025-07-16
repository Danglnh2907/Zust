package dao;

import dto.PostApprovalDTO;
import model.Account;
import model.Post;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PostApprovalDAO {
    private static final Logger LOGGER = Logger.getLogger(PostApprovalDAO.class.getName());
    private final Connection connection;

    public PostApprovalDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }
    }

    public List<PostApprovalDTO> getPendingPosts(int managerId) {
        List<PostApprovalDTO> posts = new ArrayList<>();
        String sql = """
            SELECT p.post_id, p.post_content, p.post_status, p.post_last_update,
                   a.account_id, a.username, a.avatar,
                   (SELECT COUNT(*) FROM like_post lp WHERE lp.post_id = p.post_id) AS like_count,
                   (SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id AND c.comment_status = 0) AS comment_count
            FROM post p
            JOIN account a ON p.account_id = a.account_id
            JOIN manage m ON p.group_id = m.group_id
            WHERE p.post_status = 'sent' AND m.account_id = ?
            ORDER BY p.post_create_date DESC
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, managerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("post_id"));
                    post.setPostContent(rs.getString("post_content"));
                    post.setPostStatus(rs.getString("post_status"));
//                    post.setPostLastUpdate(rs.getTimestamp("post_last_update").toLocalDateTime());

                    Account account = new Account();
                    account.setId(rs.getInt("account_id"));
                    account.setUsername(rs.getString("username"));
                    account.setAvatar(rs.getString("avatar"));
//                    account.setFullname(rs.getString("fullname"));


                    PostApprovalDTO dto = new PostApprovalDTO(post, account);
//                    dto.setLikeCount(rs.getInt("like_count"));
//                    dto.setCommentCount(rs.getInt("comment_count"));
//                    dto.setRepostCount(rs.getInt("repost_count"));

                    // Fetch images
                    String imageSql = "SELECT post_image FROM post_image WHERE post_id = ?";
                    try (PreparedStatement imageStmt = connection.prepareStatement(imageSql)) {
                        imageStmt.setInt(1, post.getId());
                        try (ResultSet imageRs = imageStmt.executeQuery()) {
                            while (imageRs.next()) {
                                dto.getImages().add(imageRs.getString("post_image"));
                            }
                        }
                    }

                    // Fetch hashtags
                    String hashtagSql = """
                        SELECT h.hashtag_name
                        FROM tag_hashtag th
                        JOIN hashtag h ON th.hashtag_id = h.hashtag_id
                        WHERE th.post_id = ? ORDER BY th.hashtag_index
                    """;
                    try (PreparedStatement hashtagStmt = connection.prepareStatement(hashtagSql)) {
                        hashtagStmt.setInt(1, post.getId());
                        try (ResultSet hashtagRs = hashtagStmt.executeQuery()) {
                            while (hashtagRs.next()) {
                                dto.getHashtags().add(hashtagRs.getString("hashtag_name"));
                            }
                        }
                    }

                    posts.add(dto);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching pending posts: " + e.getMessage());
        }

        return posts;
    }

    public boolean processPost(int postId, String action) {
        String updateSql = "UPDATE post SET post_status = ? WHERE post_id = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
            connection.setAutoCommit(false);
            updateStmt.setString(1, "approve".equalsIgnoreCase(action) ? "published" : "rejected");
            updateStmt.setInt(2, postId);

            int rowsAffected = updateStmt.executeUpdate();
            if (rowsAffected == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.severe("Error processing post: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                LOGGER.severe("Rollback failed: " + ex.getMessage());
            }
            return false;
        }
    }
}