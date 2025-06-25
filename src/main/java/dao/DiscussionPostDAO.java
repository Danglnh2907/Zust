package dao;

import dto.DiscussionPostDTO;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DiscussionPostDAO {
    private final Connection connection;

    public DiscussionPostDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }
    }

    public List<DiscussionPostDTO> getAllPostsByGroupId(int groupId) {
        List<DiscussionPostDTO> posts = new ArrayList<>();
        String sql = """
            SELECT p.post_id, p.post_content, a.username, a.avatar, p.post_last_update,
                   (SELECT COUNT(*) FROM like_post lp WHERE lp.post_id = p.post_id) AS like_count,
                   (SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id AND c.comment_status = 0) AS comment_count,
                   (SELECT COUNT(*) FROM repost r WHERE r.post_id = p.post_id) AS repost_count
            FROM post p
            JOIN account a ON p.account_id = a.account_id
            WHERE p.group_id = ? AND p.post_status = 'published'
            ORDER BY p.post_create_date DESC
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DiscussionPostDTO post = new DiscussionPostDTO();
                    post.setPostId(rs.getInt("post_id"));
                    post.setPostContent(rs.getString("post_content"));
                    post.setUsername(rs.getString("username"));
                    post.setAvatar(rs.getString("avatar"));
                    post.setLastModified(rs.getTimestamp("post_last_update").toLocalDateTime());
                    post.setLikeCount(rs.getInt("like_count"));
                    post.setCommentCount(rs.getInt("comment_count"));
                    post.setRepostCount(rs.getInt("repost_count"));

                    // Fetch images (similar to PostDAO)
                    String imageSql = "SELECT post_image FROM post_image WHERE post_id = ?";
                    try (PreparedStatement imageStmt = connection.prepareStatement(imageSql)) {
                        imageStmt.setInt(1, post.getPostId());
                        try (ResultSet imageRs = imageStmt.executeQuery()) {
                            while (imageRs.next()) {
                                post.getImages().add(imageRs.getString("post_image"));
                            }
                        }
                    }

                    // Fetch hashtags (similar to PostDAO)
                    String hashtagSql = """
                        SELECT h.hashtag_name
                        FROM tag_hashtag th
                        JOIN hashtag h ON th.hashtag_id = h.hashtag_id
                        WHERE th.post_id = ? ORDER BY th.hashtag_index
                    """;
                    try (PreparedStatement hashtagStmt = connection.prepareStatement(hashtagSql)) {
                        hashtagStmt.setInt(1, post.getPostId());
                        try (ResultSet hashtagRs = hashtagStmt.executeQuery()) {
                            while (hashtagRs.next()) {
                                post.getHashtags().add(hashtagRs.getString("hashtag_name"));
                            }
                        }
                    }

                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return posts;
    }





}