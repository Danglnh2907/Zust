package dao;

import dto.AcceptReportDTO;
import dto.ResReportPostDTO;
import dto.RespPostDTO;
import model.Account;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ReportPostDAO extends DBContext {

    private final Logger LOGGER = Logger.getLogger(RespPostDTO.class.getName());

    public List<ResReportPostDTO> getAll() {
        List<ResReportPostDTO> reportPostList = new ArrayList<>();
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
                "LEFT JOIN hashtag h ON th.hashtag_id = h.hashtag_id" +
                " WHERE rp.report_status = 'sent' AND p.post_status = 'published'";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Use a map to group results by report_id to avoid duplicates
            java.util.Map<Integer, ResReportPostDTO> reportMap = new java.util.HashMap<>();

            while (rs.next()) {
                int reportId = rs.getInt("report_id");

                // Create or get existing ReportPostDTO
                ResReportPostDTO reportPostDTO = reportMap.computeIfAbsent(reportId, k -> {
                    ResReportPostDTO dto = new ResReportPostDTO();
                    try {
                        dto.setReportId(reportId);
                        dto.setReportContent(rs.getString("report_content"));
                        dto.setReportCreateDate(rs.getTimestamp("report_create_date").toLocalDateTime());
                        dto.setReportStatus(rs.getString("report_status"));

                        // Create Account object for the reporter
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

                        // Create RespPostDTO object
                        RespPostDTO post = new RespPostDTO();
                        post.setPostId(rs.getInt("post_id"));
                        post.setPostContent(rs.getString("post_content"));
                        post.setUsername(rs.getString("poster_username")); // Use poster's username
                        post.setAvatar(rs.getString("poster_avatar")); // Use poster's avatar
                        post.setLastModified(rs.getTimestamp("post_last_update") != null ?
                                rs.getTimestamp("post_last_update").toLocalDateTime() :
                                rs.getTimestamp("post_create_date").toLocalDateTime());
                        post.setLikeCount(rs.getInt("like_count"));
                        post.setCommentCount(rs.getInt("comment_count"));
                        post.setRepostCount(rs.getInt("repost_count"));
                        // Initialize lists for images and hashtags
                        post.setImages(new ArrayList<>());
                        post.setHashtags(new ArrayList<>());
                        dto.setPost(post);

                        return dto;
                    } catch (SQLException e) {
                        throw new RuntimeException("Error mapping ReportPostDTO", e);
                    }
                });

                // Add post_image to RespPostDTO
                String postImage = rs.getString("post_image");
                if (postImage != null && !reportPostDTO.getPost().getImages().contains(postImage)) {
                    reportPostDTO.getPost().getImages().add(postImage);
                }

                // Add hashtag to RespPostDTO
                String hashtagName = rs.getString("hashtag_name");
                if (hashtagName != null && !reportPostDTO.getPost().getHashtags().contains(hashtagName)) {
                    reportPostDTO.getPost().getHashtags().add(hashtagName);
                }
            }

            // Add all ReportPostDTOs to the result list
            reportPostList.addAll(reportMap.values());

        } catch (SQLException e) {
            e.printStackTrace();
            // Consider logging the error or throwing a custom exception
        }

        return reportPostList;
    }

    public void acceptReport(AcceptReportDTO acceptReportDTO) throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        try {
            // Update report_status to 'accepted' in report_post
            String updateReportSql = "UPDATE report_post SET report_status = 'accepted' WHERE report_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateReportSql)) {
                stmt.setInt(1, acceptReportDTO.getReportId());
                stmt.executeUpdate();
            }

            // Update post_status to 'deleted' in post
            String updatePostSql = "UPDATE post SET post_status = 'deleted' WHERE post_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updatePostSql)) {
                stmt.setInt(1, acceptReportDTO.getReportedId());
                stmt.executeUpdate();
            }

            // Subtract 20 credits from the reported account
            String subtractCreditSql = "UPDATE account SET credit = credit - 5 WHERE account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(subtractCreditSql)) {
                stmt.setInt(1, acceptReportDTO.getReportedAccountId());
                stmt.executeUpdate();
            }

            // Add 3 credits to the reporter account
            String addCreditSql = "UPDATE account SET credit = credit + 1 WHERE account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(addCreditSql)) {
                stmt.setInt(1, acceptReportDTO.getReportAccountId());
                stmt.executeUpdate();
            }

            // Insert notification into notification table
            String insertNotificationSql = "INSERT INTO notification (notification_title, notification_content, notification_create_date, notification_status, account_id) " +
                    "VALUES ('Warning: Post get Deleted', ?, GETDATE(), 'sent', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertNotificationSql)) {
                stmt.setString(1, acceptReportDTO.getNotificationContent());
                stmt.setInt(2, acceptReportDTO.getReportedAccountId());
                stmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            conn.rollback(); // Rollback on error
            throw new SQLException("Failed to accept report: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(true); // Restore default auto-commit mode
        }
    }

    public void dismissReport(int id) throws SQLException {
        String sql = "UPDATE report_post SET report_status = 'rejected' WHERE report_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No report found with ID: " + id);
            }
        }
    }

    public static void main(String[] args) {
        ReportPostDAO reportPostDAO = new ReportPostDAO();
//        System.out.println(reportPostDAO.getAll().get(0).getPost());
    }
}
