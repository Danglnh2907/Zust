package dao;

import dto.ReportCommentDTO;
import dto.ReqCommentDTO;
import dto.RespCommentDTO;
import util.database.DBContext;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CommentDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public boolean createComment(ReqCommentDTO dto) {
        String sql = """
                INSERT INTO comment( \
                   comment_content, comment_image, comment_create_date, comment_last_update, \
                   account_id, post_id, reply_comment_id) \
                VALUES(?, ?, ?, ?, ?, ?, ?)""";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dto.getContent());
            stmt.setString(2, dto.getImage());
            stmt.setTimestamp(3, dto.getCreatedAt() != null ? Timestamp.valueOf(dto.getCreatedAt()) : null);
            stmt.setTimestamp(4, dto.getUpdatedAt() != null ? Timestamp.valueOf(dto.getUpdatedAt()) : null);
            stmt.setInt(5, dto.getAccountID());
            stmt.setInt(6, dto.getPostID());
            if (dto.getReplyID() == -1) {
                stmt.setNull(7, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(7, dto.getReplyID());
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("Failed to create comment: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<RespCommentDTO> getAllComments(int postID, int userID) {
        ArrayList<RespCommentDTO> comments = new ArrayList<>();
        String sql = """
                SELECT c.comment_id, c.comment_content, c.comment_image, c.comment_create_date, c.comment_last_update, c.account_id, \
                (SELECT COUNT(*) FROM like_comment WHERE comment_id = c.comment_id) AS total_likes, \
                a.username, a.avatar, c.post_id, c.reply_comment_id, \
                CAST(CASE WHEN EXISTS (SELECT 1 FROM like_comment WHERE comment_id = c.comment_id AND account_id = ?) THEN 1 ELSE 0 END AS BIT) AS is_liked \
                FROM comment c \
                JOIN account a ON a.account_id = c.account_id \
                WHERE c.post_id = ? AND c.comment_status = 0 \
                ORDER BY c.comment_last_update DESC""";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);
            stmt.setInt(2, postID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                RespCommentDTO dto = new RespCommentDTO();
                dto.setId(rs.getInt("comment_id"));
                dto.setContent(rs.getString("comment_content"));
                dto.setImage(rs.getString("comment_image"));
                dto.setCreatedAt(rs.getTimestamp("comment_create_date") == null ? null : rs.getTimestamp("comment_create_date").toLocalDateTime());
                dto.setUpdatedAt(rs.getTimestamp("comment_last_update") == null ? null : rs.getTimestamp("comment_last_update").toLocalDateTime());
                dto.setTotalLikes(rs.getInt("total_likes"));
                dto.setAccountID(rs.getInt("account_id"));
                dto.setUsername(rs.getString("username"));
                dto.setAvatar(rs.getString("avatar"));
                dto.setPostID(rs.getInt("post_id"));
                dto.setReplyID(rs.getInt("reply_comment_id") == 0 ? -1 : rs.getInt("reply_comment_id"));
                dto.setOwnComment(rs.getInt("account_id") == userID);
                dto.setLiked(rs.getBoolean("is_liked"));
                comments.add(dto);
            }
        } catch (SQLException e) {
            logger.warning("Failed to get all comments: " + e.getMessage());
        }
        return comments;
    }

    public LinkedHashMap<RespCommentDTO, ArrayList<RespCommentDTO>> filterComment(ArrayList<RespCommentDTO> comments) {
        // Create a map for efficient lookups
        Map<Integer, RespCommentDTO> commentMap = comments.stream()
                .collect(Collectors.toMap(RespCommentDTO::getId, comment -> comment));

        // Separate top-level comments and replies
        ArrayList<RespCommentDTO> topLevelComments = new ArrayList<>();
        ArrayList<RespCommentDTO> replyComments = new ArrayList<>();

        for (RespCommentDTO comment : comments) {
            if (comment.getReplyID() == -1) {
                topLevelComments.add(comment);
            } else {
                replyComments.add(comment);
            }
        }

        // Sort reply comments by last_update ascending
        replyComments.sort((c1, c2) -> {
            if (c1.getUpdatedAt() == null && c2.getUpdatedAt() == null) return 0;
            if (c1.getUpdatedAt() == null) return 1;
            if (c2.getUpdatedAt() == null) return -1;
            return c1.getUpdatedAt().compareTo(c2.getUpdatedAt());
        });

        // Create the LinkedHashMap to maintain insertion order (top-level comments order)
        LinkedHashMap<RespCommentDTO, ArrayList<RespCommentDTO>> commentHierarchy = new LinkedHashMap<>();

        // Initialize the map with top-level comments
        for (RespCommentDTO topComment : topLevelComments) {
            commentHierarchy.put(topComment, new ArrayList<>());
        }

        // Group replies under their top-level parent comments
        for (RespCommentDTO reply : replyComments) {
            RespCommentDTO topLevelParent = findTopLevelParent(reply, commentMap);
            if (topLevelParent != null && commentHierarchy.containsKey(topLevelParent)) {
                commentHierarchy.get(topLevelParent).add(reply);
            }
        }

        return commentHierarchy;
    }

    private RespCommentDTO findTopLevelParent(RespCommentDTO reply, Map<Integer, RespCommentDTO> commentMap) {
        if (reply.getReplyID() == -1) {
            // This is already a top-level comment
            return reply;
        }

        // Find the immediate parent comment from the map
        RespCommentDTO parentComment = commentMap.get(reply.getReplyID());

        if (parentComment == null) {
            // Parent comment not found, this shouldn't happen in a well-formed dataset
            return null;
        }

        // Recursively find the top-level parent
        return findTopLevelParent(parentComment, commentMap);
    }

    public boolean updateComment(int commentID, ReqCommentDTO dto) {
        String sql = """
                UPDATE comment \
                SET comment_content = ?, comment_image = ?, comment_last_update = ? \
                WHERE comment_id = ? AND account_id = ?""";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dto.getContent());
            stmt.setString(2, dto.getImage());
            stmt.setTimestamp(3, dto.getUpdatedAt() != null ? Timestamp.valueOf(dto.getUpdatedAt()) : null);
            stmt.setInt(4, commentID);
            stmt.setInt(5, dto.getAccountID());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("Failed to update comment: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteComment(int commentID, int accountID) {
        String sql = "UPDATE comment SET comment_status = 1 WHERE comment_id = ? AND account_id = ?"; //Soft delete
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, commentID);
            stmt.setInt(2, accountID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("Failed to delete comment: " + e.getMessage());
            return false;
        }
    }

    public boolean likeComment(int accountID, int commentID) {
        String sql = "INSERT INTO like_comment (account_id, comment_id) VALUES (?, ?)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountID);
            stmt.setInt(2, commentID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.info("Failed to like comment, possibly already liked: " + e.getMessage());
            return false;
        }
    }

    public boolean unlikeComment(int accountID, int commentID) {
        String sql = "DELETE FROM like_comment WHERE account_id = ? AND comment_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountID);
            stmt.setInt(2, commentID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("Failed to unlike comment: " + e.getMessage());
            return false;
        }
    }

    public boolean report(ReportCommentDTO report) {
        String sql = """
                INSERT INTO report_comment (report_content, account_id, comment_id, report_create_date, report_status) \
                VALUES (?, ?, ?, ?, ?)""";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, report.getContent());
            stmt.setInt(2, report.getAccountID());
            stmt.setInt(3, report.getCommentID());
            stmt.setTimestamp(4, Timestamp.valueOf(report.getCreatedAt()));
            stmt.setString(5, report.getStatus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("Failed to report comment: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        CommentDAO dao = new CommentDAO();
        Scanner sc = new Scanner(System.in);
        if (args.length == 1) {
            switch (args[0]) {
                case "create": {
                    //Get data
                    ReqCommentDTO dto = new ReqCommentDTO();
                    dto.setContent("This is a comment");
                    dto.setImage("profile.jpg");
                    dto.setCreatedAt(LocalDateTime.now());
                    dto.setUpdatedAt(LocalDateTime.now());
                    dto.setAccountID(1);
                    dto.setPostID(1);
                    dto.setReplyID(-1);

                    boolean success = dao.createComment(dto);
                    if (success) {
                        System.out.println("Create comment successful");
                    } else {
                        System.out.println("Create comment failed");
                    }
                    break;
                }
                case "update": {
                    ReqCommentDTO dto = new ReqCommentDTO();
                    System.out.print("Enter comment ID: ");
                    int id = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter comment content: ");
                    String content = sc.nextLine();

                    System.out.print("Enter comment image: ");
                    String image = sc.nextLine();

                    System.out.print("Enter account ID: ");
                    int accountID = sc.nextInt();

                    dto.setContent(content);
                    dto.setImage(image);
                    dto.setUpdatedAt(LocalDateTime.now());
                    dto.setAccountID(accountID);

                    boolean success = dao.updateComment(id, dto);
                    if (success) {
                        System.out.println("Update comment successful");
                    } else {
                        System.out.println("Update comment failed");
                    }
                    break;
                }
                case "delete": {
                    System.out.print("Enter comment ID: ");
                    int commentID = sc.nextInt();
                    System.out.print("Enter account ID: ");
                    int accountID = sc.nextInt();
                    boolean success = dao.deleteComment(commentID, accountID);
                    if (success) {
                        System.out.println("Delete comment successful");
                    } else {
                        System.out.println("Delete comment failed");
                    }
                    break;
                }
                case "gets": {
                    ArrayList<RespCommentDTO> comments = dao.getAllComments(1, 1);
                    for (RespCommentDTO comment : comments) {
                        System.out.println("Comment ID: " + comment.getId());
                        System.out.println("Comment image: " + (comment.getImage() == null ? "This is null value not string" : comment.getImage()));
                    }
                    break;
                }
                case "filter": {
                    ArrayList<RespCommentDTO> comments = dao.getAllComments(1, 1);
                    LinkedHashMap<RespCommentDTO, ArrayList<RespCommentDTO>> commentMap = dao.filterComment(comments);
                    System.out.println(commentMap.size());
                    commentMap.forEach((key, value) -> {
                        System.out.println(key);
                        value.forEach(System.out::println);
                    });
                    break;
                }
                case "report": {
                    System.out.print("Enter comment ID: ");
                    int commentId = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter report content: ");
                    String content = sc.nextLine();

                    ReportCommentDTO dto = new ReportCommentDTO();
                    dto.setAccountID(1);
                    dto.setCommentID(commentId);
                    dto.setContent(content);
                    dto.setCreatedAt(LocalDateTime.now());
                    dto.setStatus("sent");
                    boolean success = dao.report(dto);
                    if (success) {
                        System.out.println("Report successful");
                    } else {
                        System.out.println("Report failed");
                    }
                }
            }
        }
    }
}
