package dao;

import model.Account;
import model.ReportAccountDTO;
import model.FriendRequest;
import model.ResReportAccountDTO;
import util.database.DBContext;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

public class AccountDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public List<Account> getActiveAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = """
                SELECT * FROM account \
                WHERE account_status = 'active' AND account_role = 'user'""";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Account account = new Account();
                account.setId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setPassword(rs.getString("password"));
                account.setFullname(rs.getString("fullname"));
                account.setEmail(rs.getString("email"));
                account.setPhone(rs.getString("phone"));
                account.setGender(rs.getBoolean("gender"));
                account.setDob(rs.getDate("dob") != null
                        ? rs.getDate("dob").toLocalDate() : null);
                account.setAvatar(rs.getString("avatar"));
                account.setBio(rs.getString("bio"));
                account.setCredit(rs.getInt("credit"));
                account.setAccountStatus(rs.getString("account_status"));
                account.setAccountRole(rs.getString("account_role"));
                accounts.add(account);
            }
            return accounts;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    public Account getAccountById(int id) {
        Account account = null;
        try {
            String sql = "SELECT * FROM account WHERE account_id = ?";
            PreparedStatement stmt = new DBContext().getConnection().prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                account = new Account();
                account.setId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setPassword(rs.getString("password"));
                account.setFullname(rs.getString("fullname"));
                account.setEmail(rs.getString("email"));
                account.setPhone(rs.getString("phone"));
                account.setGender(rs.getBoolean("gender"));
                Date dobDate = rs.getDate("dob");
                if (dobDate != null) {
                    account.setDob(dobDate.toLocalDate());
                } else {
                    account.setDob(null); // or set a default date if needed
                }
                account.setAvatar(rs.getString("avatar"));
                account.setCoverImage(rs.getString("cover_image"));
                account.setBio(rs.getString("bio"));
                account.setCredit(rs.getInt("credit"));
                account.setAccountStatus(rs.getString("account_status"));
                account.setAccountRole(rs.getString("account_role"));
            }
        } catch (SQLException e) {
            logger.warning("Failed to get account by ID: " + e.getMessage());
            return null;
        }
        return account;
    }

    public boolean updateAccount(Account account) {
        try {
            String sql = """
                    UPDATE account \
                    SET fullname = ?, phone = ?, gender = ?, dob = ?, \
                        avatar = ?, cover_image = ?, bio = ? \
                    WHERE account_id = ?""";
            PreparedStatement stmt = new DBContext().getConnection().prepareStatement(sql);
            stmt.setString(1, account.getFullname());
            stmt.setString(2, account.getPhone());
            stmt.setBoolean(3, account.getGender());
            stmt.setDate(4, account.getDob() != null ? Date.valueOf(account.getDob()) : null);
            stmt.setString(5, account.getAvatar());
            stmt.setString(6, account.getCoverImage());
            stmt.setString(7, account.getBio());
            stmt.setInt(8, account.getId());
            return stmt.executeUpdate() >= 1;
        } catch (SQLException e) {
            logger.warning("Failed to update user: " + e.getMessage());
        }
        return false;
    }

    public boolean banAccount(int id) {
        logger.info("Attempting to ban account with ID: " + id);

        String sql = "UPDATE account SET account_status = 'banned' WHERE account_id = ?";
        try (
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warning("No account found with ID: " + id);
                return false;
            }
            logger.info("Successfully banned account with ID: " + id);
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to ban account with ID: " + id + " - Error: " + e.getMessage());
            return false;
        }
    }

    public boolean areFriends(int userId1, int userId2) {
        String sql = """
                SELECT 1 FROM interact \
                WHERE ((actor_account_id = ? AND target_account_id = ?) \
                OR (actor_account_id = ? AND target_account_id = ?)) AND interact_status = 'friend'""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.warning("Error checking friendship status" + e.getMessage());
        }
        return false;
    }

    public boolean isFriendRequestPending(int senderId, int receiverId) {
        String sql = """
                SELECT 1 FROM friend_request WHERE ((send_account_id = ? AND receive_account_id = ?) \
                OR (send_account_id = ? AND receive_account_id = ?)) AND friend_request_status = 'sent'""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setInt(3, receiverId);
            ps.setInt(4, senderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.warning("Error checking for pending friend request" + e.getMessage());
        }
        return false;
    }

    public void createFriendRequest(int senderId, int receiverId, String content) {
        String sql = """
                INSERT INTO friend_request (send_account_id, receive_account_id, friend_request_content) \
                VALUES (?, ?, ?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, content);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Error creating friend request" + e.getMessage());
        }
    }

    public boolean unfriend(int userId1, int userId2) {
        String sql = """
                DELETE FROM interact \
                WHERE (\
                  (actor_account_id = ? AND target_account_id = ?) OR \
                (actor_account_id = ? AND target_account_id = ?)) AND \
                interact_status = 'friend'""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.warning("Error removing friend: " + e.getMessage());
            return false;
        }
    }

    public void updateFriendRequestStatus(int requestId, String status) {
        String sql = """
                UPDATE friend_request \
                SET friend_request_status = ? \
                WHERE friend_request_id = ?""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Error updating friend request status" + e.getMessage());
        }
    }

    public void addFriend(int userId1, int userId2) {
        String sql = """
                INSERT INTO interact (actor_account_id, target_account_id, interact_status) \
                VALUES (?, ?, 'friend')""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Error adding friend" + e.getMessage());
        }
    }

    public List<FriendRequest> getFriendRequests(int userId) {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = """
                SELECT fr.*, a.username as sender_username, a.avatar as sender_avatar \
                FROM friend_request fr \
                JOIN account a ON fr.send_account_id = a.account_id \
                WHERE receive_account_id = ? AND friend_request_status = 'sent' AND a.account_status = 'active'""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FriendRequest request = new FriendRequest();
                    request.setId(rs.getInt("friend_request_id"));
                    request.setFriendRequestContent(rs.getString("friend_request_content"));
                    request.setFriendRequestDate(rs.getTimestamp("friend_request_date").toInstant());
                    request.setFriendRequestStatus(rs.getString("friend_request_status"));

                    Account sender = new Account();
                    sender.setId(rs.getInt("send_account_id"));
                    sender.setUsername(rs.getString("sender_username"));
                    sender.setAvatar(rs.getString("sender_avatar"));

                    request.setSendAccount(sender);
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            logger.warning("Error getting friend requests" + e.getMessage());
        }
        return requests;
    }

    public List<Account> getFriends(int userId) {
        List<Account> friends = new ArrayList<>();
        String sql = """
                SELECT a.* FROM account a \
                JOIN interact i ON a.account_id = i.target_account_id \
                WHERE i.actor_account_id = ? AND i.interact_status = 'friend' AND a.account_status = 'active' \
                UNION \
                SELECT a.* FROM account a \
                JOIN interact i ON a.account_id = i.actor_account_id \
                WHERE i.target_account_id = ? AND i.interact_status = 'friend' AND a.account_status = 'active'""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account friend = new Account();
                    friend.setId(rs.getInt("account_id"));
                    friend.setUsername(rs.getString("username"));
                    friend.setFullname(rs.getString("fullname"));
                    friend.setAvatar(rs.getString("avatar"));
                    friends.add(friend);
                }
            }
        } catch (SQLException e) {
            logger.warning("Error getting friends: " + e.getMessage());
        }
        return friends;
    }


    public boolean changePassword(int accountId, String currentPassword, String newHashedPassword) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Verify current password
            String sql = "SELECT password FROM account WHERE account_id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, accountId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                if (!BCrypt.checkpw(currentPassword, storedHashedPassword)) {
                    return false; // Current password incorrect
                }

                // Update password
                String updateSql = "UPDATE account SET password = ? WHERE account_id = ?";
                stmt = connection.prepareStatement(updateSql);
                stmt.setString(1, newHashedPassword);
                stmt.setInt(2, accountId);

                int rowsAffected = stmt.executeUpdate();

                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            logger.warning("Failed to change password: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }

                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.warning("Error closing resources: " + e.getMessage());
            }
        }
        return false;
    }

    public boolean report(ReportAccountDTO report) {
        String sql = """
                INSERT INTO report_account (report_content, report_account_id, reported_account_id, report_create_date, report_status) \
                VALUES (?, ?, ?, ?, ?)""";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, report.getContent());
            stmt.setInt(2, report.getReporterId());
            stmt.setInt(3, report.getReportedId());
            stmt.setTimestamp(4, Timestamp.valueOf(report.getCreatedAt()));
            stmt.setString(5, report.getStatus());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.warning("Failed to report post: " + e.getMessage());
            return false;
        }
    }

    public List<ResReportAccountDTO> getAllReports() {
        logger.info("Retrieving all sent account reports");
        List<ResReportAccountDTO> reports = new ArrayList<>();
        String sql = "SELECT ra.report_id, ra.report_account_id, ra.reported_account_id, ra.report_content, ra.report_create_date, " +
                "racc.username AS reporter_username, racc.avatar AS reporter_avatar, " +
                "ruacc.username AS reported_username, ruacc.avatar AS reported_avatar " +
                "FROM report_account ra " +
                "INNER JOIN account racc ON ra.report_account_id = racc.account_id " +
                "INNER JOIN account ruacc ON ra.reported_account_id = ruacc.account_id " +
                "WHERE ra.report_status = 'sent' AND racc.account_status = 'active' AND ruacc.account_status = 'active'";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ResReportAccountDTO report = new ResReportAccountDTO();
                report.setReportId(rs.getInt("report_id"));

                // Set reporter
                Account reporter = new Account();
                reporter.setId(rs.getInt("report_account_id"));
                reporter.setUsername(rs.getString("reporter_username"));
                reporter.setAvatar(rs.getString("reporter_avatar"));
                report.setReporter(reporter);

                // Set reported user
                Account reportedUser = new Account();
                reportedUser.setId(rs.getInt("reported_account_id"));
                reportedUser.setUsername(rs.getString("reported_username"));
                reportedUser.setAvatar(rs.getString("reported_avatar"));
                report.setReportedUser(reportedUser);

                // Set report details
                report.setReportContent(rs.getString("report_content"));
                report.setReportDate(rs.getTimestamp("report_create_date").toLocalDateTime());

                reports.add(report);
            }
            logger.info("Successfully retrieved " + reports.size() + " sent account reports");
        } catch (SQLException e) {
            logger.severe("Failed to retrieve sent account reports - Error: " + e.getMessage());
        }

        return reports;
    }

    public static void main(String[] args) {
        AccountDAO dao = new AccountDAO();
        System.out.println(dao.getAllReports());
    }
}
