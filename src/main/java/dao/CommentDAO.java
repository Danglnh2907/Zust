package dao;

import dto.ReportCommentDTO;
import dto.ReportPostDTO;
import dto.ReqCommentDTO;
import dto.RespCommentDTO;
import util.database.DBContext;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.logging.Logger;

public class CommentDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public boolean createComment(ReqCommentDTO dto) {
        Connection conn;
        try {
            //Get connection
            conn = getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false); //Start transaction

            String sql = """
                    INSERT INTO comment( \
                       comment_content, comment_image, comment_create_date, comment_last_update, \
                       account_id, post_id, reply_comment_id) \
                    VALUES(?, ?, ?, ?, ?, ?, ?)""";
            PreparedStatement stmt = conn.prepareStatement(sql);
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

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("No rows affected.");
                conn.rollback();
                return false;
            }
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public ArrayList<RespCommentDTO> getAllComments(int postID, int userID) {
        Connection conn;
        ArrayList<RespCommentDTO> comments = new ArrayList<>();
        try {
            conn = getConnection();
            if (conn == null) {
                logger.warning("No connection found");
                return null;
            }

            String sql = """
                    SELECT comment_id, comment_content, comment_image, comment_create_date, comment_last_update, c.account_id, \
                    (SELECT COUNT(*) FROM like_comment WHERE comment_id = c.comment_id) AS total_likes, \
                    c.account_id, a.username, a.avatar, post_id, reply_comment_id \
                    FROM comment c \
                    JOIN account a ON a.account_id = c.account_id \
                    WHERE c.post_id = ? AND c.comment_status = 0 \
                    ORDER BY c.comment_last_update DESC""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, postID);
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
                //SQL Server treat NULL value as 0
                dto.setReplyID(rs.getInt("reply_comment_id") == 0 ? -1 : rs.getInt("reply_comment_id"));
                dto.setOwnComment(rs.getInt("account_id") == userID);

                //Check if current requester has liked this comment
                String likeSQL = "SELECT * FROM like_comment WHERE comment_id = ? AND account_id = ?";
                PreparedStatement likeStmt = conn.prepareStatement(likeSQL);
                likeStmt.setInt(1, dto.getId());
                likeStmt.setInt(2, userID);
                dto.setLiked(likeStmt.executeQuery().next());

                comments.add(dto);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
            return null;
        }

        return comments;
    }

    public LinkedHashMap<RespCommentDTO, ArrayList<RespCommentDTO>> filterComment(ArrayList<RespCommentDTO> comments) {
        //Separate top-level comments and replies
        ArrayList<RespCommentDTO> topLevelComments = new ArrayList<>();
        ArrayList<RespCommentDTO> replyComments = new ArrayList<>();

        for (RespCommentDTO comment : comments) {
            if (comment.getReplyID() == -1) {
                topLevelComments.add(comment);
            } else {
                replyComments.add(comment);
            }
        }

        //Sort reply comments by last_update ascending
        replyComments.sort((c1, c2) -> {
            if (c1.getUpdatedAt() == null && c2.getUpdatedAt() == null) return 0;
            if (c1.getUpdatedAt() == null) return 1;
            if (c2.getUpdatedAt() == null) return -1;
            return c1.getUpdatedAt().compareTo(c2.getUpdatedAt());
        });

        //Create the LinkedHashMap to maintain insertion order (top-level comments order)
        LinkedHashMap<RespCommentDTO, ArrayList<RespCommentDTO>> commentHierarchy = new LinkedHashMap<>();

        //Initialize the map with top-level comments
        for (RespCommentDTO topComment : topLevelComments) {
            commentHierarchy.put(topComment, new ArrayList<>());
        }

        //Group replies under their top-level parent comments
        for (RespCommentDTO reply : replyComments) {
            RespCommentDTO topLevelParent = findTopLevelParent(reply, comments);
            if (topLevelParent != null) {
                commentHierarchy.get(topLevelParent).add(reply);
            }
        }

        return commentHierarchy;
    }

    private RespCommentDTO findTopLevelParent(RespCommentDTO reply, ArrayList<RespCommentDTO> allComments) {
        if (reply.getReplyID() == -1) {
            // This is already a top-level comment
            return reply;
        }

        // Find the immediate parent comment
        RespCommentDTO parentComment = null;
        for (RespCommentDTO comment : allComments) {
            if (comment.getId() == reply.getReplyID()) {
                parentComment = comment;
                break;
            }
        }

        if (parentComment == null) {
            // Parent comment not found, this shouldn't happen in a well-formed dataset
            return null;
        }

        // Recursively find the top-level parent
        return findTopLevelParent(parentComment, allComments);
    }

    public boolean updateComment(int commentID, ReqCommentDTO dto) {
        Connection conn;
        try {
            conn = getConnection();
            if (conn == null) {
                logger.warning("No connection found");
                return false;
            }
            conn.setAutoCommit(false);


            String sql = """
                    UPDATE comment \
                    SET comment_content = ?, comment_image = ?, comment_last_update = ? \
                    WHERE comment_id = ? AND account_id = ?""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, dto.getContent());
            stmt.setString(2, dto.getImage());
            stmt.setTimestamp(3, dto.getUpdatedAt() != null ? Timestamp.valueOf(dto.getUpdatedAt()) : null);
            stmt.setInt(4, commentID);
            stmt.setInt(5, dto.getAccountID());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("No rows affected.");
                logger.warning("No rows affected.");
                conn.rollback();
            }
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean deleteComment(int commentID, int accountID) {
        Connection conn;
        try {
            conn = getConnection();
            if (conn == null) {
                logger.warning("No connection found");
                return false;
            }
            conn.setAutoCommit(false);

            String sql = """
                    UPDATE comment \
                    SET comment_status = 1 \
                    WHERE comment_id = ? AND account_id = ?"""; //Soft delete
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, commentID);
            stmt.setInt(2, accountID);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("No rows affected.");
                logger.warning("No rows affected.");
            }
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean likeComment(int accountID, int commentID) {
        Connection conn;
        try {
            conn = getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);

            String sql = "INSERT INTO like_comment (account_id, comment_id) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, accountID);
            stmt.setInt(2, commentID);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("No rows affected.");
                conn.rollback();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public boolean unlikeComment(int accountID, int commentID) {
        Connection conn;
        try {
            conn = getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);

            String sql = "DELETE FROM like_comment WHERE account_id = ? AND comment_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, accountID);
            stmt.setInt(2, commentID);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("No rows affected.");
                conn.rollback();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public boolean report(ReportCommentDTO report) {
        Connection conn;
        try {
            conn = getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return false;
            }
            conn.setAutoCommit(false);

            String sql = """
                    INSERT INTO report_comment (report_content, account_id, comment_id, report_create_date, report_status) \
                    VALUES (?, ?, ?, ?, ?)""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, report.getContent());
            stmt.setInt(2, report.getAccountID());
            stmt.setInt(3, report.getCommentID());
            stmt.setTimestamp(4, Timestamp.valueOf(report.getCreatedAt()));
            stmt.setString(5, report.getStatus());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("Failed to report comment.");
                conn.rollback();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
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
