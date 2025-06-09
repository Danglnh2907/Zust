package dao;

import dto.ReqPostDTO;
import dto.RespPostsDTO;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Logger;

public class PostDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public boolean createPost(ReqPostDTO dto) throws SQLException {
        /*
         * Explain SQL flows:
         * 1: Insert the post into 'post' table
         * 2. Insert the post into 'image' table
         * 3. Check if the hashtag already exist in the 'hashtag' table
         * 3.1. If yes, only insert into to the 'tag_hashtag' table
         * 3.2. If no, insert the new hashtags into the 'hashtag' table before insert into 'tag_hashtag' table
         */

        Connection conn = null;
        try {
            //Get connection
            conn = getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false); //Start transaction

            //Insert into post table
            String postSql = "INSERT INTO post (post_content, account_id, post_create_date, post_last_update, post_privacy, post_status, group_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement postStmt = conn.prepareStatement(postSql, PreparedStatement.RETURN_GENERATED_KEYS);
            postStmt.setString(1, dto.getPostContent());
            postStmt.setInt(2, dto.getAccountID());
            postStmt.setTimestamp(3, dto.getCreatedAt() != null ? Timestamp.valueOf(dto.getCreatedAt()) : null);
            postStmt.setTimestamp(4, dto.getLastModified() != null ? Timestamp.valueOf(dto.getLastModified()) : null);
            postStmt.setString(5, dto.getPrivacy());
            postStmt.setString(6, dto.getStatus());
            if (dto.getGroupID() == -1) {
                postStmt.setNull(7, java.sql.Types.INTEGER);
            } else {
                postStmt.setInt(7, dto.getGroupID());
            }
            int affectedRows = postStmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            //Get generated post_id
            ResultSet rs = postStmt.getGeneratedKeys();
            int postId;
            if (rs.next()) {
                postId = rs.getInt(1);
            } else {
                conn.rollback();
                return false;
            }

            //Insert into post_image table
            String imageSql = "INSERT INTO post_image (post_image, post_id) VALUES (?, ?)";
            PreparedStatement imageStmt = conn.prepareStatement(imageSql);
            ArrayList<String> images = dto.getImages();
            for (String imagePath : images) {
                if (imagePath != null && !imagePath.isEmpty()) {
                    imageStmt.setString(1, imagePath);
                    imageStmt.setInt(2, postId);
                    imageStmt.addBatch();
                }
            }
            imageStmt.executeBatch();

            // Insert into hashtag and tag_hashtag tables
            String hashtagSql = "MERGE INTO hashtag AS target " +
                    "USING (SELECT ? AS hashtag_name) AS source " +
                    "ON target.hashtag_name = source.hashtag_name " +
                    "WHEN NOT MATCHED THEN " +
                    "INSERT (hashtag_name) VALUES (source.hashtag_name);";
            String tagHashtagSql = "INSERT INTO tag_hashtag (hashtag_index, post_id, hashtag_id) " +
                    "SELECT ?, ?, hashtag_id FROM hashtag WHERE hashtag_name = ?";
            PreparedStatement hashtagStmt = conn.prepareStatement(hashtagSql);
            PreparedStatement tagHashtagStmt = conn.prepareStatement(tagHashtagSql);
            ArrayList<String> hashtags = dto.getHashtags();
            for (int i = 0; i < hashtags.size(); i++) {
                String hashtag = hashtags.get(i);
                if (hashtag != null && !hashtag.trim().isEmpty()) {
                    //Insert or skip if hashtag exists
                    hashtagStmt.setString(1, hashtag.trim());
                    hashtagStmt.executeUpdate();

                    //Link hashtag to post
                    tagHashtagStmt.setInt(1, i); // hashtag_index
                    tagHashtagStmt.setInt(2, postId);
                    tagHashtagStmt.setString(3, hashtag.trim());
                    tagHashtagStmt.addBatch();
                }
            }
            tagHashtagStmt.executeBatch();

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public ArrayList<RespPostsDTO> getPosts(int userID) throws SQLException {
        String sql = "SELECT p.post_id, p.post_content, a.username, p.post_last_update, " +
                "(SELECT COUNT(*) FROM like_post lp WHERE lp.post_id = p.post_id) AS like_count, " +
                "(SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id AND c.comment_status = 0) AS comment_count, " +
                "(SELECT COUNT(*) FROM repost r WHERE r.post_id = p.post_id) AS repost_count " +
                "FROM post p JOIN account a ON p.account_id = a.account_id " +
                "WHERE p.post_status = 'published' ORDER BY p.post_create_date DESC";
        ArrayList<RespPostsDTO> posts = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                RespPostsDTO post = new RespPostsDTO();
                post.setPostId(rs.getInt("post_id"));
                post.setPostContent(rs.getString("post_content"));
                post.setUsername(rs.getString("username"));
                post.setLastModified(rs.getTimestamp("post_last_update") != null
                        ? rs.getTimestamp("post_last_update").toLocalDateTime() : null);
                post.setLikeCount(rs.getInt("like_count"));
                post.setCommentCount(rs.getInt("comment_count"));
                post.setRepostCount(rs.getInt("repost_count"));

                // Fetch images
                String imageSql = "SELECT post_image FROM post_image WHERE post_id = ?";
                try (PreparedStatement imageStmt = conn.prepareStatement(imageSql)) {
                    imageStmt.setInt(1, post.getPostId());
                    ResultSet imageRs = imageStmt.executeQuery();
                    while (imageRs.next()) {
                        post.getImages().add(imageRs.getString("post_image"));
                    }
                }

                // Fetch hashtags
                String hashtagSql = "SELECT h.hashtag_name " +
                        "FROM tag_hashtag th JOIN hashtag h ON th.hashtag_id = h.hashtag_id " +
                        "WHERE th.post_id = ? ORDER BY th.hashtag_index";
                try (PreparedStatement hashtagStmt = conn.prepareStatement(hashtagSql)) {
                    hashtagStmt.setInt(1, post.getPostId());
                    ResultSet hashtagRs = hashtagStmt.executeQuery();
                    while (hashtagRs.next()) {
                        post.getHashtags().add(hashtagRs.getString("hashtag_name"));
                    }
                }

                posts.add(post);
            }

            return posts;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return posts;
        }
    }

    public static void main(String[] args) {
        PostDAO dao = new PostDAO();
        try {
            ArrayList<RespPostsDTO> posts = dao.getPosts(1);
            for (RespPostsDTO post : posts) {
                System.out.println(post.getPostContent());
            }
        } catch (SQLException e) {
            System.out.printf("SQLException: %s%n", e.getMessage());
        }
    }
}
