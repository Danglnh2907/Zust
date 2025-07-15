package dao;

import dto.ReportPostDTO;
import dto.ReqPostDTO;
import dto.RespPostDTO;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class PostDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public boolean createPost(ReqPostDTO dto) {
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
            conn = new DBContext().getConnection();;
            if (conn == null) {
                logger.warning("No connection available");
                return false;
            }
            conn.setAutoCommit(false); //Start transaction

            //Insert into post table
            String postSql = """
                    INSERT INTO post\
                     (post_content, account_id, post_create_date, post_last_update, post_privacy, post_status, group_id)\
                     VALUES (?, ?, ?, ?, ?, ?, ?)""";
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

            //Insert into hashtag and tag_hashtag tables
            updateHashtagsForPost(conn, postId, dto.getHashtags());

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.warning("Failed to rollback transaction: " + ex.getMessage());
            }
            return false;
        }
    }

    /**
     * Centralized method to handle the creation and linking of hashtags to a post.
     *
     * @param conn     The active database connection (with transaction).
     * @param postId   The ID of the post to link hashtags to.
     * @param hashtags The list of hashtag strings.
     * @throws SQLException if a database access error occurs.
     */
    private void updateHashtagsForPost(Connection conn, int postId, ArrayList<String> hashtags) throws SQLException {
        if (hashtags == null || hashtags.isEmpty()) {
            return;
        }
        String hashtagSql = """
                MERGE INTO hashtag AS target \
                USING (SELECT ? AS hashtag_name) AS source \
                ON target.hashtag_name = source.hashtag_name \
                WHEN NOT MATCHED THEN \
                INSERT (hashtag_name) VALUES (source.hashtag_name);""";
        String tagHashtagSql = """
                INSERT INTO tag_hashtag (hashtag_index, post_id, hashtag_id) \
                SELECT ?, ?, hashtag_id FROM hashtag WHERE hashtag_name = ?""";
        try (PreparedStatement hashtagStmt = conn.prepareStatement(hashtagSql);
             PreparedStatement tagHashtagStmt = conn.prepareStatement(tagHashtagSql)) {

            for (int i = 0; i < hashtags.size(); i++) {
                String hashtag = hashtags.get(i);
                if (hashtag != null && !hashtag.trim().isEmpty()) {
                    String trimmedHashtag = hashtag.trim();
                    //Insert or skip if hashtag exists
                    hashtagStmt.setString(1, trimmedHashtag);
                    hashtagStmt.executeUpdate();

                    //Link hashtag to post
                    tagHashtagStmt.setInt(1, i); //hashtag_index
                    tagHashtagStmt.setInt(2, postId);
                    tagHashtagStmt.setString(3, trimmedHashtag);
                    tagHashtagStmt.addBatch();
                }
            }
            tagHashtagStmt.executeBatch();
        }
    }

    /**
     * Fetches and populates the hashtags for a given post.
     *
     * @param post The post DTO to populate.
     * @param conn The active database connection.
     * @throws SQLException if a database access error occurs.
     */
    private void fetchHashtagsForPost(RespPostDTO post, Connection conn) throws SQLException {
        String hashtagSql = """
                SELECT h.hashtag_name \
                FROM tag_hashtag th JOIN hashtag h ON th.hashtag_id = h.hashtag_id \
                WHERE th.post_id = ? ORDER BY th.hashtag_index""";
        try (PreparedStatement hashtagStmt = conn.prepareStatement(hashtagSql)) {
            hashtagStmt.setInt(1, post.getPostId());
            ResultSet hashtagRs = hashtagStmt.executeQuery();
            while (hashtagRs.next()) {
                post.getHashtags().add(hashtagRs.getString("hashtag_name"));
            }
        }
    }

    /**
     * Get a post based on postID, reusing an existing database connection.
     *
     * @param postId - The ID of the post
     * @param userID - ID of the user who request this.
     * @param conn   - The existing database connection to use.
     * @return RespPostDTO object corresponding to the post
     */
    private RespPostDTO getPost(int postId, int userID, Connection conn) {
        String sql = """
                SELECT p.post_id, p.post_content, p.account_id, p.repost_post_id, a.username, a.avatar, p.post_last_update,\
                (SELECT COUNT(*) FROM like_post lp WHERE lp.post_id = p.post_id) AS like_count, \
                (SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id AND c.comment_status = 0) AS comment_count,\
                (SELECT COUNT(*) FROM post WHERE repost_post_id = p.post_id AND post_status = 'published') AS repost_count,\
                CAST(CASE WHEN EXISTS (SELECT 1 FROM like_post WHERE post_id = p.post_id AND account_id = ?) THEN 1 ELSE 0 END AS BIT) AS is_liked \
                FROM post p JOIN account a ON p.account_id = a.account_id \
                WHERE p.post_status = 'published' AND p.post_id = ?""";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPost(rs, userID, conn);
            }
        } catch (SQLException e) {
            logger.warning("Failed to get post with existing connection: " + e.getMessage());
        }
        return null;
    }

    /**
     * Maps a row from a ResultSet to a RespPostDTO object, including its related entities.
     *
     * @param rs     The ResultSet, positioned at the correct row.
     * @param userID The ID of the user making the request, for context-specific fields like 'isLiked'.
     * @param conn   The active database connection to use for fetching related entities.
     * @return A fully populated RespPostDTO.
     * @throws SQLException if a database access error occurs.
     */
    private RespPostDTO mapResultSetToPost(ResultSet rs, int userID, Connection conn) throws SQLException {
        RespPostDTO post = new RespPostDTO();
        int postId = rs.getInt("post_id");
        post.setPostId(postId);
        post.setPostContent(rs.getString("post_content"));
        post.setAccountID(rs.getInt("account_id"));
        post.setUsername(rs.getString("username"));
        post.setAvatar(rs.getString("avatar"));
        post.setLastModified(rs.getTimestamp("post_last_update") != null
                ? rs.getTimestamp("post_last_update").toLocalDateTime() : null);
        post.setLikeCount(rs.getInt("like_count"));
        post.setCommentCount(rs.getInt("comment_count"));
        post.setRepostCount(rs.getInt("repost_count"));
        post.setOwnPost(rs.getInt("account_id") == userID);
        post.setLiked(rs.getBoolean("is_liked"));

        //Fetch images
        String imageSql = "SELECT post_image FROM post_image WHERE post_id = ?";
        try (PreparedStatement imageStmt = conn.prepareStatement(imageSql)) {
            imageStmt.setInt(1, postId);
            ResultSet imageRs = imageStmt.executeQuery();
            while (imageRs.next()) {
                post.getImages().add(imageRs.getString("post_image"));
            }
        }

        //Fetch hashtags
        fetchHashtagsForPost(post, conn);

        int repostId = rs.getInt("repost_post_id");
        if (repostId > 0) {
            // This recursive call is now safe as it reuses the same connection.
            RespPostDTO repostedPost = getPost(repostId, userID, conn);
            post.setRepost(repostedPost);
        }

        return post;
    }

    /**
     * Method to get a list of posts based on conditions
     *
     * @param userID    - ID of the user who request this. This is used to track if the current requester has
     *                  the right to view those post (authorization), check for like status,...
     * @param accountID - ID of the account who posted these posts
     * @return - ArrayList of RespPostDTO
     */
    public ArrayList<RespPostDTO> getPosts(int userID, int accountID) {
        ArrayList<RespPostDTO> posts = new ArrayList<>();
        String sql = """
                SELECT p.post_id, p.account_id, p.post_content, a.username, a.avatar, p.post_last_update, p.repost_post_id, \
                (SELECT COUNT(*) FROM like_post lp WHERE lp.post_id = p.post_id) AS like_count, \
                (SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id AND c.comment_status = 0) AS comment_count, \
                (SELECT COUNT(*) FROM post WHERE repost_post_id = p.post_id AND post_status = 'published') AS repost_count, \
                CAST(CASE WHEN EXISTS (SELECT 1 FROM like_post WHERE post_id = p.post_id AND account_id = ?) THEN 1 ELSE 0 END AS BIT) AS is_liked \
                FROM post p JOIN account a ON p.account_id = a.account_id \
                WHERE p.post_status = 'published' AND p.account_id = ? ORDER BY p.post_create_date DESC""";

        try (Connection conn = new DBContext().getConnection();) {
            if (conn == null) {
                logger.warning("No connection available");
                return posts; // Return empty list
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userID);
                stmt.setInt(2, accountID);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs, userID, conn));
                }
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
        return posts;
    }

    /**
     * Get a post based on postID
     *
     * @param postId - The ID of the post
     * @param userID - ID of the user who request this. This is used to track if the current requester has
     *               the right to view those post (authorization), check for like status,...
     * @return RespPostDTO object corresponding to the post
     */
    public RespPostDTO getPost(int postId, int userID) {
        try (Connection conn = new DBContext().getConnection();) {
            if (conn == null) {
                logger.warning("No connection available");
                return null;
            }
            return getPost(postId, userID, conn);
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    /**
     * Method to get a list of posts for a user's newsfeed.
     * This includes posts from the user, their friends, and groups they've joined.
     *
     * @param userID - ID of the user requesting the newsfeed.
     * @return - ArrayList of RespPostDTO for the newsfeed.
     */
    public ArrayList<RespPostDTO> getNewsfeedPosts(int userID) {
        ArrayList<RespPostDTO> posts = new ArrayList<>();
        String sql = """
                SELECT p.post_id, p.account_id, p.post_content, a.username, a.avatar, p.post_last_update, p.repost_post_id, \
                       (SELECT COUNT(*) FROM like_post lp WHERE lp.post_id = p.post_id) AS like_count, \
                       (SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id AND c.comment_status = 0) AS comment_count, \
                       (SELECT COUNT(*) FROM post WHERE repost_post_id = p.post_id AND post_status = 'published') AS repost_count, \
                       CAST(CASE WHEN EXISTS (SELECT 1 FROM like_post WHERE post_id = p.post_id AND account_id = ?) THEN 1 ELSE 0 END AS BIT) AS is_liked \
                FROM post p JOIN account a ON p.account_id = a.account_id \
                WHERE p.post_status = 'published' AND \
                      (\
                          /* User's own posts */ \
                          p.account_id = ? \
                          /* Posts from friends (public or friend-only) */ \
                          OR (p.account_id IN (SELECT target_account_id FROM interact WHERE actor_account_id = ? AND interact_status = 'friend' \
                                               UNION \
                                               SELECT actor_account_id FROM interact WHERE target_account_id = ? AND interact_status = 'friend') \
                              AND p.post_privacy IN ('public', 'friend') AND p.group_id IS NULL) \
                          /* Posts from joined groups */ \
                          OR (p.group_id IN (SELECT group_id FROM participate WHERE account_id = ?)) \
                          /* All public posts from anyone */ \
                          OR (p.post_privacy = 'public' AND p.group_id IS NULL) \
                      )\
                ORDER BY p.post_create_date DESC""";

        try (Connection conn = new DBContext().getConnection();) {
            if (conn == null) {
                logger.warning("No connection available");
                return posts;
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userID);
                stmt.setInt(2, userID);
                stmt.setInt(3, userID);
                stmt.setInt(4, userID);
                stmt.setInt(5, userID);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs, userID, conn));
                }
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
        return posts;
    }

    /**
     * Method to get a list of posts in a group
     *
     * @param userID  - ID of the user requesting the posts
     * @param groupID - ID of the group
     * @return - ArrayList of RespPostDTO for the group feed.
     */
    public ArrayList<RespPostDTO> getPostsInGroup(int userID, int groupID) {
        ArrayList<RespPostDTO> posts = new ArrayList<>();
        String SQL = """
                SELECT p.post_id, p.account_id, p.post_content, a.username, a.avatar, p.post_last_update, p.repost_post_id, \
                (SELECT COUNT(*) FROM like_post lp WHERE lp.post_id = p.post_id) AS like_count, \
                (SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id AND c.comment_status = 0) AS comment_count, \
                (SELECT COUNT(*) FROM post WHERE repost_post_id = p.post_id AND post_status = 'published') AS repost_count, \
                CAST(CASE WHEN EXISTS (SELECT 1 FROM like_post WHERE post_id = p.post_id AND account_id = ?) THEN 1 ELSE 0 END AS BIT) AS is_liked \
                FROM post p JOIN account a ON p.account_id = a.account_id \
                WHERE p.post_status = 'published' AND p.group_id = ? ORDER BY p.post_create_date DESC""";

        try (Connection conn = new DBContext().getConnection();) {
            if (conn == null) {
                logger.warning("No connection available!");
                return posts;
            }
            try (PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setInt(1, userID);
                stmt.setInt(2, groupID);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs, userID, conn));
                }
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }

        return posts;
    }

    public boolean editPost(int postId, ReqPostDTO postDTO) {
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();;
            if (conn == null) {
                logger.warning("No connection available");
                return false;
            }
            conn.setAutoCommit(false); // Start transaction

            // Update post table
            String postSql = """
                    UPDATE post SET post_content = ?, post_privacy = ?, post_last_update = ?, post_status = ? \
                    WHERE post_id = ? AND account_id = ? AND post_status = 'published'""";
            try (PreparedStatement stmt = conn.prepareStatement(postSql)) {
                stmt.setString(1, postDTO.getPostContent());
                stmt.setString(2, postDTO.getPrivacy() != null ? postDTO.getPrivacy() : "public");
                stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(4, postDTO.getStatus() != null ? postDTO.getStatus() : "published");
                stmt.setInt(5, postId);
                stmt.setInt(6, postDTO.getAccountID());
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return false; // Post not found or unauthorized
                }
            }

            // Delete existing images
            String deleteImageSql = "DELETE FROM post_image WHERE post_id = ?";
            try (PreparedStatement deleteImageStmt = conn.prepareStatement(deleteImageSql)) {
                deleteImageStmt.setInt(1, postId);
                deleteImageStmt.executeUpdate();
            }

            // Insert new images
            String imageSql = "INSERT INTO post_image (post_image, post_id) VALUES (?, ?)";
            try (PreparedStatement imageStmt = conn.prepareStatement(imageSql)) {
                ArrayList<String> images = postDTO.getImages();
                for (String imagePath : images) {
                    if (imagePath != null && !imagePath.trim().isEmpty()) {
                        imageStmt.setString(1, imagePath);
                        imageStmt.setInt(2, postId);
                        imageStmt.addBatch();
                    }
                }
                imageStmt.executeBatch();
            }

            // Delete existing hashtags
            String deleteTagHashtagSql = "DELETE FROM tag_hashtag WHERE post_id = ?";
            try (PreparedStatement deleteTagStmt = conn.prepareStatement(deleteTagHashtagSql)) {
                deleteTagStmt.setInt(1, postId);
                deleteTagStmt.executeUpdate();
            }

            //Insert new hashtags
            updateHashtagsForPost(conn, postId, postDTO.getHashtags());

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.warning("Failed to rollback transaction: " + ex.getMessage());
            }
            return false;
        }
    }

    public boolean deletePost(int postId, int accountId) {
        String postSql = """
                UPDATE post SET post_status = 'deleted', post_last_update = ? \
                WHERE post_id = ? AND account_id = ? AND post_status = 'published'""";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(postSql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, postId);
            stmt.setInt(3, accountId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public boolean likePost(int postId, int accountId) {
        String sql = "INSERT INTO like_post (post_id, account_id) VALUES (?, ?)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, accountId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.info("Failed to like post, possibly already liked: " + e.getMessage());
            return false;
        }
    }

    public boolean unlikePost(int postId, int accountId) {
        String sql = "DELETE FROM like_post WHERE post_id = ? AND account_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, accountId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.warning("Failed to unlike post: " + e.getMessage());
            return false;
        }
    }

    public boolean repost(int postId, int accountId) {
        String sql = "INSERT INTO post (post_content, account_id, post_create_date, post_last_update, post_privacy, post_status, repost_post_id) VALUES ('', ?, ?, ?, 'public', 'published', ?)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, postId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.warning("Failed to repost: " + e.getMessage());
            return false;
        }
    }

    public boolean report(ReportPostDTO report) {
        String sql = """
                INSERT INTO report_post (report_content, account_id, post_id, report_create_date, report_status) \
                VALUES (?, ?, ?, ?, ?)""";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, report.getContent());
            stmt.setInt(2, report.getAccountID());
            stmt.setInt(3, report.getPostID());
            stmt.setTimestamp(4, Timestamp.valueOf(report.getCreatedAt()));
            stmt.setString(5, report.getStatus());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.warning("Failed to report post: " + e.getMessage());
            return false;
        }
    }

    public int getAccountIdByPostId(int postId) throws SQLException {
        String sql = "SELECT account_id FROM post WHERE post_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("account_id");
                }
            }
        }
        return 0; // Return 0 if no account is found
    }

    public List<RespPostDTO> getPendingPosts(int accountId, int groupId) {
        logger.info("Retrieving pending posts for account ID: " + accountId + " and group ID: " + groupId);
        ArrayList<RespPostDTO> posts = new ArrayList<>();
        String SQL = """
                SELECT p.post_id, p.account_id, p.post_content, a.username, a.avatar, p.post_last_update, p.repost_post_id, \
                (SELECT COUNT(*) FROM like_post lp WHERE lp.post_id = p.post_id) AS like_count, \
                (SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id AND c.comment_status = 0) AS comment_count, \
                (SELECT COUNT(*) FROM post WHERE repost_post_id = p.post_id AND post_status = 'published') AS repost_count, \
                CAST(CASE WHEN EXISTS (SELECT 1 FROM like_post WHERE post_id = p.post_id AND account_id = ?) THEN 1 ELSE 0 END AS BIT) AS is_liked \
                FROM post p JOIN account a ON p.account_id = a.account_id \
                WHERE p.post_status = 'sent' AND a.account_id = ? AND p.group_id = ? ORDER BY p.post_create_date DESC""";

        try {
            if (connection == null) {
                logger.warning("No connection available!");
                return posts;
            }
            try (PreparedStatement stmt = connection.prepareStatement(SQL)) {
                stmt.setInt(1, accountId);
                stmt.setInt(2, accountId);
                stmt.setInt(3, groupId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs, accountId, connection));
                }
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }

        return posts;
    }

    public static void main(String[] args) {
        PostDAO dao = new PostDAO();
        Scanner sc = new Scanner(System.in);
        if (args.length == 1) {
            switch (args[0]) {
                case "create": {
                    //Get data
                    ReqPostDTO dto = new ReqPostDTO();
                    dto.setAccountID(1); //Hardcode
                    dto.setCreatedAt(LocalDateTime.now());
                    dto.setLastModified(LocalDateTime.now());
                    dto.setPrivacy("public"); //Hardcode
                    dto.setStatus("published"); //Hardcode
                    dto.setGroupID(-1); //Hardcode

                    //Get content from user (no image/hashtags for now)
                    System.out.print("Enter post content: ");
                    String content = sc.nextLine();
                    dto.setPostContent(content);

                    boolean success = dao.createPost(dto);
                    if (success) {
                        System.out.println("Create post successful");
                    } else {
                        System.out.println("Create post failed");
                    }
                    break;
                }
                case "update": {
                    //Read post ID
                    System.out.print("Enter post ID: ");
                    int postId = sc.nextInt();
                    sc.nextLine(); //Swallow '\n' in the input buffer

                    //Read post content
                    System.out.print("Enter post content: ");
                    String content = sc.nextLine();

                    //Read post status -> Normally, this should be changed by group manager/admin, but test just in case
                    System.out.print("Enter new status: ");
                    String status = sc.nextLine();

                    //Read post privacy
                    System.out.print("Enter new privacy: ");
                    String privacy = sc.nextLine();

                    ReqPostDTO dto = new ReqPostDTO();
                    dto.setAccountID(1); //Hardcode
                    //dto.setCreatedAt(LocalDateTime.now()); -> Edit method ignore this field, so setting it or not don't matter
                    dto.setLastModified(LocalDateTime.now());
                    dto.setPrivacy(privacy); //Hardcode
                    dto.setStatus(status); //Hardcode
                    dto.setGroupID(-1); //Hardcode
                    dto.setPostContent(content);

                    boolean success = dao.editPost(postId, dto);
                    if (success) {
                        System.out.println("Update post successful");
                    } else {
                        System.out.println("Update post failed");
                    }
                    break;
                }
                case "delete": {
                    System.out.print("Enter post ID: ");
                    int postId = sc.nextInt();
                    boolean success = dao.deletePost(postId, 1); //Hardcode account ID for now
                    if (success) {
                        System.out.println("Delete post successful");
                    } else {
                        System.out.println("Delete post failed");
                    }
                    break;
                }
                case "gets": {
                    ArrayList<RespPostDTO> posts = dao.getPosts(1, 1);
                    for (RespPostDTO post : posts) {
                        System.out.printf("Post content: %s\n", post.getPostContent()); //You can choose to print more
                    }
                    break;
                }
                case "get": {
                    //Get post ID
                    System.out.print("Enter post ID: ");
                    int postId = sc.nextInt();

                    //Get account ID
                    System.out.print("Enter account ID: ");
                    int accountId = sc.nextInt();

                    RespPostDTO post = dao.getPost(postId, accountId);
                    if (post != null) {
//                        System.out.printf("Post content: %s\n", post.getPostContent());
                        System.out.println(post);
                    } else {
                        System.out.println("Post not found");
                    }
                    break;
                }
                case "like": {
                    System.out.print("Enter post ID: ");
                    int postId = sc.nextInt();
                    boolean success = dao.likePost(postId, 1);
                    if (success) {
                        System.out.println("Like post successful");
                    } else {
                        System.out.println("Like post failed");
                    }
                    break;
                }
                case "report": {
                    System.out.print("Enter post ID: ");
                    int postId = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter report content: ");
                    String content = sc.nextLine();

                    ReportPostDTO dto = new ReportPostDTO();
                    dto.setAccountID(1);
                    dto.setPostID(postId);
                    dto.setContent(content);
                    dto.setCreatedAt(LocalDateTime.now());
                    dto.setStatus("sent");
                    boolean success = dao.report(dto);
                    if (success) {
                        System.out.println("Report successful");
                    }
                    else {
                        System.out.println("Report failed");
                    }
                    break;
                }
                case "newsfeed": {
                    System.out.print("Enter user ID for newsfeed: ");
                    int userId = sc.nextInt();
                    ArrayList<RespPostDTO> posts = dao.getNewsfeedPosts(userId);
                    if (posts != null) {
                        for (RespPostDTO post : posts) {
                            System.out.printf("Post by %s: %s (Liked: %b)\n", post.getUsername(), post.getPostContent(), post.isLiked());
                        }
                    }
                    else {
                        System.out.println("Could not retrieve newsfeed.");
                    }
                    break;
                }
            }
        }

    }
}
