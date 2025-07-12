package dao;

import dto.AcceptGroupReportDTO;
import dto.ResGroupReportPostDTO;
import dto.RespPostDTO;
import model.Account;
import util.database.DBContext;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class ReportGroupPostDAO {

    private final Logger LOGGER = Logger.getLogger(RespPostDTO.class.getName());

    public List<ResGroupReportPostDTO> getAllReportsForGroupManager(int groupId, int managerAccountId) throws SQLException {
        boolean isManager = checkIfManager(groupId, managerAccountId);
        if (!isManager) {
            throw new SQLException("Account ID " + managerAccountId + " is not the manager of group ID " + groupId);
        }

        List<ResGroupReportPostDTO> reportPostList = new ArrayList<>();
        String sql = "SELECT rp.report_id, rp.report_content, rp.report_create_date, rp.report_status, " +
                "ra.account_id AS reporter_account_id, ra.username AS reporter_username, ra.password AS reporter_password, ra.fullname AS reporter_fullname, ra.email AS reporter_email, ra.phone AS reporter_phone, ra.gender AS reporter_gender, ra.dob AS reporter_dob, ra.avatar AS reporter_avatar, ra.bio AS reporter_bio, ra.credit AS reporter_credit, ra.account_status AS reporter_account_status, ra.account_role AS reporter_account_role, " +
                "pa.account_id AS poster_account_id, pa.username AS poster_username, pa.avatar AS poster_avatar, " +
                "p.post_id, p.post_content, p.post_create_date, p.post_last_update, p.post_privacy, p.post_status, " +
                "pi.post_image, h.hashtag_name, " +
                "(SELECT COUNT(*) FROM like_post lp WHERE lp.post_id = p.post_id) AS like_count, " +
                "(SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id AND c.comment_status = 0) AS comment_count, " +
                "(SELECT COUNT(*) FROM post rp WHERE rp.repost_post_id = p.post_id AND rp.post_status = 'published') AS repost_count " +
                "FROM report_post rp " +
                "INNER JOIN account ra ON rp.account_id = ra.account_id " +
                "INNER JOIN post p ON rp.post_id = p.post_id " +
                "INNER JOIN account pa ON p.account_id = pa.account_id " +
                "LEFT JOIN post_image pi ON p.post_id = pi.post_id " +
                "LEFT JOIN tag_hashtag th ON p.post_id = th.post_id " +
                "LEFT JOIN hashtag h ON th.hashtag_id = h.hashtag_id " +
                "WHERE rp.report_status = 'sent' AND p.post_status = 'published' AND p.group_id = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                Map<Integer, ResGroupReportPostDTO> reportMap = new HashMap<>();

                while (rs.next()) {
                    int reportId = rs.getInt("report_id");

                    ResGroupReportPostDTO reportPostDTO = reportMap.computeIfAbsent(reportId, k -> {
                        try {
                            ResGroupReportPostDTO dto = new ResGroupReportPostDTO();
                            dto.setReportId(reportId);
                            dto.setReportContent(rs.getString("report_content"));
                            dto.setReportCreateDate(rs.getTimestamp("report_create_date").toLocalDateTime());
                            dto.setReportStatus(rs.getString("report_status"));

                            Account reporterAccount = new Account();
                            reporterAccount.setId(rs.getInt("reporter_account_id"));
                            reporterAccount.setUsername(rs.getString("reporter_username"));
                            reporterAccount.setPassword(rs.getString("reporter_password"));
                            reporterAccount.setFullname(rs.getString("reporter_fullname"));
                            reporterAccount.setEmail(rs.getString("reporter_email"));
                            reporterAccount.setPhone(rs.getString("reporter_phone"));
                            reporterAccount.setGender(rs.getBoolean("reporter_gender"));
                            reporterAccount.setDob(rs.getDate("reporter_dob") != null ? rs.getDate("reporter_dob").toLocalDate() : null);
                            reporterAccount.setAvatar(rs.getString("reporter_avatar"));
                            reporterAccount.setBio(rs.getString("reporter_bio"));
                            reporterAccount.setCredit(rs.getInt("reporter_credit"));
                            reporterAccount.setAccountStatus(rs.getString("reporter_account_status"));
                            reporterAccount.setAccountRole(rs.getString("reporter_account_role"));
                            dto.setAccount(reporterAccount);

                            RespPostDTO post = new RespPostDTO();
                            post.setPostId(rs.getInt("post_id"));
                            post.setPostContent(rs.getString("post_content"));
                            post.setUsername(rs.getString("poster_username"));
                            post.setAvatar(rs.getString("poster_avatar"));
                            post.setLastModified(rs.getTimestamp("post_last_update") != null ?
                                    rs.getTimestamp("post_last_update").toLocalDateTime() :
                                    rs.getTimestamp("post_create_date").toLocalDateTime());
                            post.setLikeCount(rs.getInt("like_count"));
                            post.setCommentCount(rs.getInt("comment_count"));
                            post.setRepostCount(rs.getInt("repost_count"));
                            post.setImages(new ArrayList<>());
                            post.setHashtags(new ArrayList<>());
                            dto.setPost(post);

                            return dto;
                        } catch (SQLException e) {
                            throw new RuntimeException("Error mapping ResGroupReportPostDTO", e);
                        }
                    });

                    String postImage = rs.getString("post_image");
                    if (postImage != null && !reportPostDTO.getPost().getImages().contains(postImage)) {
                        reportPostDTO.getPost().getImages().add(postImage);
                    }

                    String hashtagName = rs.getString("hashtag_name");
                    if (hashtagName != null && !reportPostDTO.getPost().getHashtags().contains(hashtagName)) {
                        reportPostDTO.getPost().getHashtags().add(hashtagName);
                    }
                }

                reportPostList.addAll(reportMap.values());
            }
        }

        return reportPostList;
    }

    public Map<String, Object> getGroupInfoById(int groupId) throws SQLException {
        Map<String, Object> groupInfo = new HashMap<>();
        String sql = "SELECT name, description, image FROM `group` WHERE group_id = ?";  // Adjust table/column nếu khác
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    groupInfo.put("name", rs.getString("name"));
                    groupInfo.put("description", rs.getString("description"));
                    groupInfo.put("image", rs.getString("image"));
                    return groupInfo;
                }
            }
        }
        return null;  // Không tìm thấy
    }
    private boolean checkIfManager(int groupId, int accountId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM manage WHERE group_id = ? AND account_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void acceptReportForGroup(AcceptGroupReportDTO acceptReportDTO, int managerAccountId) throws SQLException {
        int postId = acceptReportDTO.getReportedPostId();
        int groupId = getGroupIdFromPost(postId);
        if (groupId == 0 || !checkIfManager(groupId, managerAccountId)) {
            throw new SQLException("Not authorized to accept report for this post.");
        }

        try (Connection conn = new DBContext().getConnection()) {
            conn.setAutoCommit(false);

            try {
                String updateReportSql = "UPDATE report_post SET report_status = 'accepted' WHERE report_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateReportSql)) {
                    stmt.setInt(1, acceptReportDTO.getReportId());
                    stmt.executeUpdate();
                }

                String updatePostSql = "UPDATE post SET post_status = 'deleted' WHERE post_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updatePostSql)) {
                    stmt.setInt(1, postId);
                    stmt.executeUpdate();
                }

                String insertNotificationSql = "INSERT INTO notification (notification_title, notification_content, notification_create_date, notification_status, account_id) " +
                        "VALUES ('Post get Deleted', ?, GETDATE(), 'sent', ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertNotificationSql)) {
                    stmt.setString(1, acceptReportDTO.getNotificationContent());
                    stmt.setInt(2, acceptReportDTO.getReportedAccountId());
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Failed to accept report: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private int getGroupIdFromPost(int postId) throws SQLException {
        String sql = "SELECT group_id FROM post WHERE post_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("group_id");
                }
            }
        }
        return 0;
    }

    public void dismissReportForGroup(int reportId, int managerAccountId) throws SQLException {
        int postId = getPostIdFromReport(reportId);
        int groupId = getGroupIdFromPost(postId);
        if (groupId == 0 || !checkIfManager(groupId, managerAccountId)) {
            throw new SQLException("Not authorized to dismiss report for this post.");
        }

        String sql = "UPDATE report_post SET report_status = 'rejected' WHERE report_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reportId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No report found with ID: " + reportId);
            }
        }
    }

    private int getPostIdFromReport(int reportId) throws SQLException {
        String sql = "SELECT post_id FROM report_post WHERE report_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reportId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("post_id");
                }
            }
        }
        return 0;
    }
}
