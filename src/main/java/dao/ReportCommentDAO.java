package dao;

import model.ResReportCommentDTO;
import model.Account;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ReportCommentDAO extends DBContext {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public List<ResReportCommentDTO> getAll() {
        logger.info("Retrieving all sent comment reports");
        List<ResReportCommentDTO> reports = new ArrayList<>();
        String sql = """
                SELECT cr.report_id, cr.account_id, cr.comment_id, cr.report_content, cr.report_create_date, 
                c.comment_content, c.comment_image, c.account_id AS commenter_id, 
                ra.username AS reporter_username, ra.Fullname AS reporter_Fullname, ra.avatar AS reporter_avatar,
                ca.username AS commenter_username, ca.Fullname AS commenter_Fullname, ca.avatar AS commenter_avatar
                FROM report_comment cr
                INNER JOIN comment c ON cr.comment_id = c.comment_id
                INNER JOIN account ra ON cr.account_id = ra.account_id
                INNER JOIN account ca ON c.account_id = ca.account_id
                WHERE cr.report_status = 'sent' AND ra.account_status = 'active' AND ca.account_status = 'active' AND c.comment_status = 0""";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ResReportCommentDTO report = new ResReportCommentDTO();
                report.setId(rs.getInt("report_id"));
                report.setCommentId(rs.getInt("comment_id"));

                // Set reporter
                Account reporter = new Account();
                reporter.setId(rs.getInt("account_id"));
                reporter.setUsername(rs.getString("reporter_username"));
                reporter.setFullname(rs.getString("reporter_Fullname"));
                reporter.setAvatar(rs.getString("reporter_avatar"));
                report.setReporter(reporter);

                // Set commenter
                Account commenter = new Account();
                commenter.setId(rs.getInt("commenter_id"));
                commenter.setUsername(rs.getString("commenter_username"));
                commenter.setFullname(rs.getString("commenter_Fullname"));
                commenter.setAvatar(rs.getString("commenter_avatar"));
                report.setCommenter(commenter);

                // Set comment details
                report.setCommentContent(rs.getString("comment_content"));
                report.setCommentImage(rs.getString("comment_image"));
                report.setReportContent(rs.getString("report_content"));
                report.setReportDate(rs.getTimestamp("report_create_date"));

                reports.add(report);
            }
            logger.info("Successfully retrieved " + reports.size() + " sent comment reports");
        } catch (SQLException e) {
            logger.severe("Failed to retrieve sent comment reports - Error: " + e.getMessage());
        }

        return reports;
    }

    public boolean acceptReport(int id) {
        logger.info("Accepting comment report ID: " + id);
        String updateReportSql = "UPDATE report_comment SET report_status = 'accepted' WHERE report_id = ?";


        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Update report status
            try (PreparedStatement stmt = conn.prepareStatement(updateReportSql)) {
                stmt.setInt(1, id);
                if (stmt.executeUpdate() == 0) {
                    logger.warning("No report found with ID: " + id);
                    conn.rollback();
                    return false;
                }
            }
            conn.commit();
            logger.info("Successfully accepted report ID: " + id);
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to accept report ID: " + id + " - Error: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.severe("Failed to rollback transaction - Error: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.severe("Failed to close connection - Error: " + closeEx.getMessage());
                }
            }
        }
    }

    public boolean dismissReport(int id) {
        logger.info("Dismissing comment report ID: " + id);
        String sql = "UPDATE report_comment SET report_status = 'rejected' WHERE report_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warning("No report found with ID: " + id);
                return false;
            }
            logger.info("Successfully dismissed report ID: " + id);
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to dismiss report ID: " + id + " - Error: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        ReportCommentDAO dao = new ReportCommentDAO();
//        AcceptReportDTO dto = new AcceptReportDTO();
//        dto.setReportId(1);
//        dto.setReportedAccountId(1);
//        dto.setReportAccountId(1);
//        dto.setReportedId(1);
//        dto.setNotificationContent("xoa xoa");
//        System.out.println(dao.acceptReport(dto));
    }
}
