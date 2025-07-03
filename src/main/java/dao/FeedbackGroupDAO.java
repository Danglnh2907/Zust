package dao;

import dto.FeedbackGroupDTO;
import model.Account;
import model.FeedbackGroup;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FeedbackGroupDAO extends DBContext {
    private static final Logger LOGGER = Logger.getLogger(FeedbackGroupDAO.class.getName());
    private final Connection connection;

    public FeedbackGroupDAO() {
        this.connection = new DBContext().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Database connection is null");
        }
    }

    private static final String SELECT_FEEDBACK_BY_GROUP_ID =
            "SELECT fg.feedback_group_id, fg.feedback_group_content, fg.report_create_date, fg.report_status, " +
                    "a.account_id, a.username, a.avatar, a.fullname " +
                    "FROM feedback_group fg " +
                    "JOIN account a ON fg.account_id = a.account_id " +
                    "WHERE fg.group_id = ? AND fg.report_status = 'sent' " +
                    "ORDER BY fg.report_create_date DESC";

    public List<FeedbackGroupDTO> getFeedbacksByGroupId(int groupId, String status) {
        List<FeedbackGroupDTO> feedbacks = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_FEEDBACK_BY_GROUP_ID)) {
            stmt.setInt(1, groupId);
//            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FeedbackGroup feedback = new FeedbackGroup();
                    feedback.setId(rs.getInt("feedback_group_id"));
                    feedback.setFeedbackGroupContent(rs.getString("feedback_group_content"));
//             feedback.setReportCreateDate(rs.getObject("report_create_date", LocalDateTime.class));
                    feedback.setReportStatus(rs.getString("report_status"));

                    Account account = new Account();
                    account.setId(rs.getInt("account_id"));
                    account.setUsername(rs.getString("username"));
                    account.setAvatar(rs.getString("avatar"));
                    account.setFullname(rs.getString("fullname"));

                    FeedbackGroupDTO dto = new FeedbackGroupDTO(feedback, account);
                    feedbacks.add(dto);
                    LOGGER.info("Fetched " + feedbacks.size() + " feedbacks for groupId: " + groupId);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching feedbacks: " + e.getMessage());
        }
        return feedbacks;
    }


//    public boolean processFeedback(int feedbackId, String action) {
//        String updateSql = "UPDATE feedback_group SET report_status = ? WHERE feedback_group_id = ?";
//        try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
//            connection.setAutoCommit(false);
//            updateStmt.setBoolean(1, "0".equalsIgnoreCase(action));
//            updateStmt.setInt(2, feedbackId);
//
//            int rowsAffected = updateStmt.executeUpdate();
//            if (rowsAffected == 0) {
//                connection.rollback();
//                return false;
//            }
//
//            connection.commit();
//            return true;
//        } catch (SQLException e) {
//            LOGGER.severe("Error processing feedback: " + e.getMessage());
//            try {
//                connection.rollback();
//            } catch (SQLException ex) {
//                LOGGER.severe("Rollback failed: " + ex.getMessage());
//            }
//            return false;
//        }
//    }
}