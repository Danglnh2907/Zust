package dao;

import dto.ReqCommentDTO;
import dto.RespCommentDTO;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Logger;

public class CommentDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public boolean createComment(ReqCommentDTO dto) {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);

            String permissionSql = """
                SELECT p.post_privacy, p.post_status
                FROM post p
                WHERE p.post_id = ? AND p.post_status = 'published'
                AND (
                    p.post_privacy = 'public'
                    OR (p.post_privacy = 'friend' AND EXISTS (
                        SELECT 1 FROM interact i
                        WHERE i.actor_account_id = ? AND i.target_account_id = p.account_id
                        AND i.interact_status = 'friend'
                    ))
                    OR (p.post_privacy = 'private' AND p.account_id = ?)
                )""";
            try (PreparedStatement permStmt = conn.prepareStatement(permissionSql)) {
                permStmt.setInt(1, dto.getPostID());
                permStmt.setInt(2, dto.getAccountID());
                permStmt.setInt(3, dto.getAccountID());
                ResultSet rs = permStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    logger.warning("Post not found or user lacks permission");
                    return false;
                }
            }

            if (dto.getReplyCommentId() != null) {
                String replySql = "SELECT comment_status FROM comment WHERE comment_id = ? AND comment_status = 0";
                try (PreparedStatement replyStmt = conn.prepareStatement(replySql)) {
                    replyStmt.setInt(1, dto.getReplyCommentId());
                    ResultSet rs = replyStmt.executeQuery();
                    if (!rs.next()) {
                        conn.rollback();
                        logger.warning("Reply comment not found or deleted");
                        return false;
                    }
                }
            }

            String commentSql = """
                INSERT INTO comment (comment_content, comment_image, comment_status, comment_create_date, account_id, post_id, reply_comment_id)
                VALUES (?, ?, 0, ?, ?, ?, ?)""";
            try (PreparedStatement commentStmt = conn.prepareStatement(commentSql)) {
                commentStmt.setString(1, dto.getCommentContent());
                commentStmt.setString(2, dto.getCommentImage());
                commentStmt.setTimestamp(3, Timestamp.valueOf(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now()));
                commentStmt.setInt(4, dto.getAccountID());
                commentStmt.setInt(5, dto.getPostID());
                if (dto.getReplyCommentId() != null) {
                    commentStmt.setInt(6, dto.getReplyCommentId());
                } else {
                    commentStmt.setNull(6, java.sql.Types.INTEGER);
                }
                int affectedRows = commentStmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning("Create comment error: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                logger.warning("Rollback error: " + ex.getMessage());
            }
            return false;
        }
    }

    public boolean editComment(int commentId, ReqCommentDTO dto) {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);

            String checkSql = """
                SELECT c.comment_status, p.post_status
                FROM comment c
                JOIN post p ON c.post_id = p.post_id
                WHERE c.comment_id = ? AND c.account_id = ? AND c.comment_status = 0 AND p.post_status = 'published'""";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, commentId);
                checkStmt.setInt(2, dto.getAccountID());
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    logger.warning("Comment not found, deleted, or unauthorized");
                    return false;
                }
            }

            if (dto.getReplyCommentId() != null) {
                String replySql = "SELECT comment_status FROM comment WHERE comment_id = ? AND comment_status = 0";
                try (PreparedStatement replyStmt = conn.prepareStatement(replySql)) {
                    replyStmt.setInt(1, dto.getReplyCommentId());
                    ResultSet rs = replyStmt.executeQuery();
                    if (!rs.next()) {
                        conn.rollback();
                        logger.warning("Reply comment not found or deleted");
                        return false;
                    }
                }
            }

            String commentSql = """
                UPDATE comment
                SET comment_content = ?, comment_image = ?, comment_last_update = ?, reply_comment_id = ?
                WHERE comment_id = ? AND account_id = ? AND comment_status = 0""";
            try (PreparedStatement commentStmt = conn.prepareStatement(commentSql)) {
                commentStmt.setString(1, dto.getCommentContent());
                commentStmt.setString(2, dto.getCommentImage());
                commentStmt.setTimestamp(3, Timestamp.valueOf(dto.getLastModified() != null ? dto.getLastModified() : LocalDateTime.now()));
                if (dto.getReplyCommentId() != null) {
                    commentStmt.setInt(4, dto.getReplyCommentId());
                } else {
                    commentStmt.setNull(4, java.sql.Types.INTEGER);
                }
                commentStmt.setInt(5, commentId);
                commentStmt.setInt(6, dto.getAccountID());
                int affectedRows = commentStmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning("Edit comment error: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                logger.warning("Rollback error: " + ex.getMessage());
            }
            return false;
        }
    }

    public ArrayList<RespCommentDTO> viewAllComments(int postId, int viewerAccountId) {
        ArrayList<RespCommentDTO> comments = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) {
                return comments;
            }

            String permissionSql = """
            SELECT p.post_privacy, p.post_status
            FROM post p
            WHERE p.post_id = ? AND p.post_status = 'published'
            AND (
                p.post_privacy = 'public'
                OR (p.post_privacy = 'friend' AND EXISTS (
                    SELECT 1 FROM interact i
                    WHERE i.actor_account_id = ? AND i.target_account_id = p.account_id
                    AND i.interact_status = 'friend'
                ))
                OR (p.post_privacy = 'private' AND p.account_id = ?)
            )""";
            try (PreparedStatement permStmt = conn.prepareStatement(permissionSql)) {
                permStmt.setInt(1, postId);
                permStmt.setInt(2, viewerAccountId);
                permStmt.setInt(3, viewerAccountId);
                ResultSet rs = permStmt.executeQuery();
                if (!rs.next()) {
                    logger.warning("Post not found or viewer lacks permission");
                    return comments;
                }
            }

            String commentSql = """
            SELECT c.comment_id, c.comment_content, c.comment_image, c.comment_create_date,
                   c.comment_last_update, c.reply_comment_id, a.username, a.account_id,
                   (SELECT COUNT(*) FROM like_comment lc WHERE lc.comment_id = c.comment_id) AS like_count,
                   c.post_id
            FROM comment c
            JOIN account a ON c.account_id = a.account_id
            WHERE c.post_id = ? AND c.comment_status = 0
            ORDER BY c.comment_create_date DESC""";
            try (PreparedStatement commentStmt = conn.prepareStatement(commentSql)) {
                commentStmt.setInt(1, postId);
                ResultSet rs = commentStmt.executeQuery();
                while (rs.next()) {
                    RespCommentDTO comment = new RespCommentDTO();
                    comment.setCommentId(rs.getInt("comment_id"));
                    comment.setCommentContent(rs.getString("comment_content"));
                    comment.setCommentImage(rs.getString("comment_image"));
                    comment.setUsername(rs.getString("username"));
                    comment.setCreatedAt(rs.getTimestamp("comment_create_date") != null
                            ? rs.getTimestamp("comment_create_date").toLocalDateTime() : null);
                    comment.setLastModified(rs.getTimestamp("comment_last_update") != null
                            ? rs.getTimestamp("comment_last_update").toLocalDateTime() : null);
                    comment.setReplyCommentId(rs.getInt("reply_comment_id") != 0
                            ? rs.getInt("reply_comment_id") : null);
                    comment.setLikeCount(rs.getInt("like_count"));
                    comment.setAccountId(rs.getInt("account_id"));
                    comment.setPostId(rs.getInt("post_id")); // Set postId
                    comments.add(comment);
                }
            }

            return comments;
        } catch (SQLException e) {
            logger.warning("View comments error: " + e.getMessage());
            return comments;
        }
    }

    public boolean deleteComment(int commentId, int accountId) {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);

            String checkSql = """
                SELECT c.comment_status, p.post_status
                FROM comment c
                JOIN post p ON c.post_id = p.post_id
                WHERE c.comment_id = ? AND c.account_id = ? AND c.comment_status = 0 AND p.post_status = 'published'""";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, commentId);
                checkStmt.setInt(2, accountId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    logger.warning("Comment not found, deleted, or unauthorized");
                    return false;
                }
            }

            String deleteSql = """
                UPDATE comment
                SET comment_status = 1, comment_last_update = ?
                WHERE comment_id = ? AND account_id = ? AND comment_status = 0""";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                deleteStmt.setInt(2, commentId);
                deleteStmt.setInt(3, accountId);
                int affectedRows = deleteStmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning("Delete comment error: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                logger.warning("Rollback error: " + ex.getMessage());
            }
            return false;
        }
    }

    public RespCommentDTO getCommentById(int commentId) {
        Connection conn = null;
        RespCommentDTO comment = null;
        try {
            conn = getConnection();
            if (conn == null) {
                logger.warning("Failed to get database connection");
                return null;
            }

            String sql = """
            SELECT c.comment_id, c.comment_content, c.comment_image, c.comment_create_date,
                   c.comment_last_update, c.reply_comment_id, a.username, a.account_id,
                   (SELECT COUNT(*) FROM like_comment lc WHERE lc.comment_id = c.comment_id) AS like_count,
                   c.post_id
            FROM comment c
            JOIN account a ON c.account_id = a.account_id
            WHERE c.comment_id = ? AND c.comment_status = 0""";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, commentId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    comment = new RespCommentDTO();
                    comment.setCommentId(rs.getInt("comment_id"));
                    comment.setCommentContent(rs.getString("comment_content"));
                    comment.setCommentImage(rs.getString("comment_image"));
                    comment.setUsername(rs.getString("username"));
                    comment.setCreatedAt(rs.getTimestamp("comment_create_date") != null
                            ? rs.getTimestamp("comment_create_date").toLocalDateTime() : null);
                    comment.setLastModified(rs.getTimestamp("comment_last_update") != null
                            ? rs.getTimestamp("comment_last_update").toLocalDateTime() : null);
                    comment.setReplyCommentId(rs.getInt("reply_comment_id") != 0
                            ? rs.getInt("reply_comment_id") : null);
                    comment.setLikeCount(rs.getInt("like_count"));
                    comment.setAccountId(rs.getInt("account_id"));
                    comment.setPostId(rs.getInt("post_id")); // Set postId
                }
            }
        } catch (SQLException e) {
            logger.warning("Error fetching comment by ID: " + e.getMessage());
        }
        return comment;
    }

    public int getLatestCommentId(int accountId) {
        Connection conn = null;
        int latestCommentId = -1;
        try {
            conn = getConnection();
            if (conn == null) {
                logger.warning("Failed to get database connection");
                return -1;
            }

            String sql = """
            SELECT MAX(comment_id) AS latest_comment_id
            FROM comment
            WHERE account_id = ? AND comment_status = 0""";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, accountId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    latestCommentId = rs.getInt("latest_comment_id");
                }
            }
        } catch (SQLException e) {
            logger.warning("Error fetching latest comment ID: " + e.getMessage());
        }
        return latestCommentId;
    }
}