package dao;

import model.Account;
import model.GroupCommentReportDTO;
import util.database.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GroupCommentReportDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public List<GroupCommentReportDTO> getByGroupId(int groupId) {
        List<GroupCommentReportDTO> reports = new ArrayList<>();

        String sql = """
            SELECT rc.report_id, rc.account_id AS reporter_id, rc.comment_id, rc.report_content, rc.report_create_date,
                   c.comment_content, c.comment_image, c.account_id AS commenter_id,
                   ra.username AS reporter_username, ra.fullname AS reporter_fullname, ra.avatar AS reporter_avatar,
                   ca.username AS commenter_username, ca.fullname AS commenter_fullname, ca.avatar AS commenter_avatar
            FROM report_comment rc
            JOIN comment c ON rc.comment_id = c.comment_id
            JOIN post p ON c.post_id = p.post_id
            JOIN account ra ON rc.account_id = ra.account_id
            JOIN account ca ON c.account_id = ca.account_id
            WHERE rc.report_status = 'sent'
              AND ra.account_status = 'active'
              AND ca.account_status = 'active'
              AND c.comment_status = 0
              AND p.group_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    GroupCommentReportDTO dto = new GroupCommentReportDTO();
                    dto.setReportId(rs.getInt("report_id"));
                    dto.setCommentId(rs.getInt("comment_id"));
                    dto.setCommentContent(rs.getString("comment_content"));
                    dto.setCommentImage(rs.getString("comment_image"));
                    dto.setReportMessage(rs.getString("report_content"));
                    dto.setReportDate(rs.getTimestamp("report_create_date"));

                    Account reporter = new Account();
                    reporter.setId(rs.getInt("reporter_id"));
                    reporter.setUsername(rs.getString("reporter_username"));
                    reporter.setFullname(rs.getString("reporter_fullname"));
                    reporter.setAvatar(rs.getString("reporter_avatar"));
                    dto.setReporter(reporter);
                    dto.setReportAccountId(reporter.getId());

                    Account commenter = new Account();
                    commenter.setId(rs.getInt("commenter_id"));
                    commenter.setUsername(rs.getString("commenter_username"));
                    commenter.setFullname(rs.getString("commenter_fullname"));
                    commenter.setAvatar(rs.getString("commenter_avatar"));
                    dto.setCommenter(commenter);
                    dto.setReportedAccountId(commenter.getId());

                    reports.add(dto);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error fetching comment reports by group ID: " + e.getMessage());
        }

        return reports;
    }

    public boolean acceptReport(GroupCommentReportDTO dto) {
        String updateReportSql = "UPDATE report_comment SET report_status = 'accepted' WHERE report_id = ?";
        String updateCommentSql = "UPDATE comment SET comment_status = 1 WHERE comment_id = ?";
        String reduceCreditSql = "UPDATE account SET credit = credit - 5 WHERE account_id = ?";
        String increaseCreditSql = "UPDATE account SET credit = credit + 1 WHERE account_id = ?";
        String insertNotificationSql = """
            INSERT INTO notification (account_id, notification_title, notification_content, notification_create_date)
            VALUES (?, 'Comment Removed', ?, GETDATE())
        """;

        try (Connection conn = new DBContext().getConnection()) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement stmt1 = conn.prepareStatement(updateReportSql);
                    PreparedStatement stmt2 = conn.prepareStatement(updateCommentSql);
                    PreparedStatement stmt3 = conn.prepareStatement(reduceCreditSql);
                    PreparedStatement stmt4 = conn.prepareStatement(increaseCreditSql);
                    PreparedStatement stmt5 = conn.prepareStatement(insertNotificationSql)
            ) {
                stmt1.setInt(1, dto.getReportId());
                if (stmt1.executeUpdate() == 0) { conn.rollback(); return false; }

                stmt2.setInt(1, dto.getCommentId());
                if (stmt2.executeUpdate() == 0) { conn.rollback(); return false; }

                stmt3.setInt(1, dto.getReportedAccountId());
                stmt4.setInt(1, dto.getReportAccountId());

                stmt5.setInt(1, dto.getReportedAccountId());
                stmt5.setString(2, dto.getNotificationContent());

                stmt3.executeUpdate();
                stmt4.executeUpdate();
                stmt5.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.severe("Accept failed: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            logger.severe("Database error in acceptReport: " + e.getMessage());
            return false;
        }
    }

    public boolean dismissReport(int reportId) {
        String sql = "UPDATE report_comment SET report_status = 'rejected' WHERE report_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reportId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Dismiss report failed: " + e.getMessage());
            return false;
        }
    }
}
