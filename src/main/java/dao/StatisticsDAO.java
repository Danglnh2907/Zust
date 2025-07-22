package dao;

import model.Account;
import model.Group;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class StatisticsDAO {
    /*
     * Get platform statistics and calculate ranking
     * 1. Get total users (active)
     * 2. Get total group (active)
     * 3. Get pending reports
     * 4. Get number of posts in 24h
     * 5. Get top 10 most active users in the month
     * 6. Get top 10 most active groups in the month
     */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public int getTotalUsers() {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return -1;
            }

            String sql = """
                    SELECT COUNT(*) AS total_user FROM account WHERE account_status = 'active' AND account_role = 'user'""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int total = rs.getInt("total_user");
            if (total > 0) {
                logger.info("Total active users: " + total);
            } else {
                logger.warning("Found no active users");
            }
            return total;
        } catch (Exception e) {
            logger.warning("Error getting total users + " + e.getMessage());
            return -1;
        }
    }

    public int getTotalGroups() {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return -1;
            }

            String sql = """
                    SELECT COUNT(*) AS total_group FROM "group" WHERE group_status = 'active'""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int total = rs.getInt("total_group");
            if (total > 0) {
                logger.info("Total active groups: " + total);
            } else {
                logger.warning("Found no active groups");
            }
            return total;
        } catch (Exception e) {
            logger.warning("Error getting total groups + " + e.getMessage());
            return -1;
        }
    }

    public int getTotalPendingReports() {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return -1;
            }

            String sql = """
                    SELECT 
                        (SELECT COUNT(*) FROM report_account WHERE report_status = 'sent') +
                        (SELECT COUNT(*) FROM report_post WHERE report_status = 'sent') +
                        (SELECT COUNT(*) FROM report_comment WHERE report_status = 'sent') AS total_report""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int total = rs.getInt("total_report");
            if (total > 0) {
                logger.info("Total pending reports: " + total);
            } else {
                logger.warning("Found no pending reports");
            }
            return total;
        } catch (Exception e) {
            logger.warning("Error getting total pending reports + " + e.getMessage());
            return -1;
        }
    }

    public int getTotalPostsIn24Hours() {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return -1;
            }

            String sql = """
                    SELECT COUNT(*) AS total_post_in_24_hrs FROM post \
                    WHERE post_status = 'published' AND post_create_date >= DATEADD("HOUR", -24, GETDATE())""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int total = rs.getInt("total_post_in_24_hrs");
            if (total > 0) {
                logger.info("Total posts in 24 hrs : " + total);
            } else {
                logger.warning("Found no posts in 24 hrs");
            }
            return total;
        } catch (Exception e) {
            logger.warning("Error getting total posts in 24 hours " + e.getMessage());
            return -1;
        }
    }

    public HashMap<Account, Integer> getTop10Accounts() {
        Connection conn;
        HashMap<Account, Integer> tops = new LinkedHashMap<>();
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return tops;
            }

            String sql = """
                    SELECT TOP 10 a.account_id, a.username, a.avatar, (
                              	0.3 * ISNULL(p.total_post, 0) +
                                  0.2 * ISNULL(c.total_comment, 0) +
                                  0.1 * ISNULL(pr.total_repost, 0) +
                                  0.4 * (
                                      ISNULL(ra.total_report_account, 0) +
                                      ISNULL(rp.total_report_post, 0) +
                                      ISNULL(rc.total_report_comment, 0)
                                  )) * 100 AS point
                    FROM account a
                    LEFT JOIN (
                        SELECT account_id, COUNT(*) AS total_post FROM post
                        WHERE post_status = 'published' AND repost_post_id IS NULL AND MONTH(post_create_date) = MONTH(GETDATE())
                        GROUP BY account_id
                    ) p ON a.account_id = p.account_id
                    LEFT JOIN (
                        SELECT account_id, COUNT(*) AS total_repost FROM post
                        WHERE post_status = 'published' AND repost_post_id IS NOT NULL AND MONTH(post_create_date) = MONTH(GETDATE())
                        GROUP BY account_id
                    ) pr ON a.account_id = pr.account_id
                    LEFT JOIN (
                        SELECT account_id, COUNT(*) AS total_comment FROM comment
                        WHERE comment_status = 0 AND MONTH(comment_create_date) = MONTH(GETDATE())
                        GROUP BY account_id
                    ) c ON a.account_id = c.account_id
                    LEFT JOIN (
                        SELECT report_account_id AS account_id, COUNT(*) AS total_report_account FROM report_account
                        WHERE report_status = 'accepted' AND MONTH(report_create_date) = MONTH(GETDATE())
                        GROUP BY report_account_id
                    ) ra ON a.account_id = ra.account_id
                    LEFT JOIN (
                        SELECT account_id, COUNT(*) AS total_report_post FROM report_post
                        WHERE report_status = 'accepted' AND MONTH(report_create_date) = MONTH(GETDATE())
                        GROUP BY account_id
                    ) rp ON a.account_id = rp.account_id
                    LEFT JOIN (
                        SELECT account_id, COUNT(*) AS total_report_comment FROM report_comment
                        WHERE report_status = 'accepted' AND MONTH(report_create_date) = MONTH(GETDATE())
                        GROUP BY account_id
                    ) rc ON a.account_id = rc.account_id
                    WHERE a.account_role = 'user'
                    ORDER BY point DESC""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Account account = new Account();
                account.setId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setAvatar(rs.getString("avatar"));
                tops.put(account, rs.getInt("point"));
            }
        } catch (Exception e) {
            logger.warning("Error getting top 10 active accounts " + e.getMessage());
        }
        return tops;
    }

    public HashMap<Group, Integer> getTop10Groups() {
        Connection conn;
        HashMap<Group, Integer> tops = new LinkedHashMap<>();
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return tops;
            }

            String sql = """
                    SELECT TOP 10 g.group_id, g.group_name, g.group_cover_image, (0.45 * ISNULL(p.total_post, 0) + 0.55 * ISNULL(pa.total_member, 0)) * 100 AS point
                    FROM "group" g
                    LEFT JOIN (
                        SELECT group_id, COUNT(*) AS total_member FROM participate
                        WHERE MONTH(participate_start_date) = MONTH(GETDATE())
                        GROUP BY group_id
                    ) pa ON g.group_id = pa.group_id
                    LEFT JOIN (
                        SELECT group_id, COUNT(*) AS total_post FROM post
                        WHERE post_status = 'published' AND MONTH(post_create_date) = MONTH(GETDATE())
                        GROUP BY group_id
                    ) p ON g.group_id= p.group_id
                    WHERE g.group_status = 'active'
                    ORDER BY point DESC""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Group group = new Group();
                group.setId(rs.getInt("group_id"));
                group.setGroupName(rs.getString("group_name"));
                group.setGroupCoverImage(rs.getString("group_cover_image"));
                tops.put(group, rs.getInt("point"));
            }
        } catch (Exception e) {
            logger.warning("Error getting top 10 groups " + e.getMessage());
        }
        return tops;
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            StatisticsDAO dao = new StatisticsDAO();
            switch (args[0]) {
                case "total_users" ->
                    System.out.println("Total active users: " + dao.getTotalUsers());
                case "total_group" ->
                    System.out.println("Total active groups: " + dao.getTotalGroups());
                case "total_pending_reports" ->
                    System.out.println("Total pending reports: " + dao.getTotalPendingReports());
                case "total_post_in_24_hours" ->
                    System.out.println("Total posts in 24 hrs : " + dao.getTotalPostsIn24Hours());
                case "user_ranking" -> {
                    HashMap<Account, Integer> accounts = dao.getTop10Accounts();
                    for (Map.Entry<Account, Integer> entry : accounts.entrySet()) {
                        System.out.println(entry.getKey().getUsername() + ": " + entry.getValue());
                    }
                }
                case "group_ranking" -> {
                    HashMap<Group, Integer> groups = dao.getTop10Groups();
                    for (Map.Entry<Group, Integer> entry : groups.entrySet()) {
                        System.out.println(entry.getKey().getGroupName() + ": " + entry.getValue());
                    }
                }
            }
        }
    }
}
