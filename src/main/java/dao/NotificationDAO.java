package dao;

import model.Notification;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;

public class NotificationDAO {
    private final Logger logger = Logger.getLogger(NotificationDAO.class.getName());

    public boolean addNotification(Notification notification) {
        Connection conn;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return false;
            }
            conn.setAutoCommit(false);

            String sql = """
                    INSERT INTO notification (notification_title, notification_content, notification_create_date, notification_status, account_id)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, notification.getTitle());
            stmt.setString(2, notification.getContent());
            stmt.setTimestamp(3, notification.getCreateDate() != null ? Timestamp.valueOf(notification.getCreateDate()) : null);
            stmt.setString(4, notification.getStatus());
            stmt.setInt(5, notification.getAccountId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("Failed to add notification. No rows affected.");
                conn.rollback();
                return false;
            }
            logger.warning("Successfully added notification.");
            conn.commit();
            return true;
        } catch (Exception e) {
            logger.warning("Failed to add notification " + e.getMessage());
            return false;
        }
    }

    public ArrayList<Notification> getNotifications(int userId) {
        Connection conn = null;
        ArrayList<Notification> notifications = new ArrayList<>();
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return notifications;
            }

            String sql = """
                    SELECT n.*, a.username, a.avatar FROM notification n
                    JOIN account a ON a.account_id = ? ORDER BY n.notification_create_date DESC""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Notification notification = new Notification();
                notification.setId(rs.getInt("notification_id"));
                notification.setTitle(rs.getString("notification_title"));
                notification.setContent(rs.getString("notification_content"));
                notification.setCreateDate(rs.getTimestamp("notification_create_date").toLocalDateTime());
                notification.setStatus(rs.getString("notification_status"));
                notification.setSender(rs.getString("username"));
                notification.setSenderAvatar(rs.getString("avatar"));
                notifications.add(notification);
            }
        } catch (Exception e) {
            logger.warning("Failed to get notifications from database " + e.getMessage());
        }
        return notifications;
    }

    public boolean markNotificationAsRead(int notificationId) {
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return false;
            }
            conn.setAutoCommit(false);

            String sql = """
                    UPDATE notification SET notification_status = 'read' WHERE notification_id = ?""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, notificationId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("Failed to update notification. No rows affected.");
                conn.rollback();
                return false;
            }
            logger.warning("Successfully updated notification.");
            conn.commit();
            return true;
        } catch (Exception e) {
            logger.warning("Failed to mark notification " + e.getMessage());
            return false;
        }
    }

    public boolean markAllNotificationAsRead(int userId) {
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                logger.warning("No connection available");
                return false;
            }

            String sql = """
                    UPDATE notification SET notification_status = 'read' WHERE account_id = ?""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.warning("Failed to update notification. No rows affected.");
                conn.rollback();
                return false;
            }
            logger.warning("Successfully updated notification.");
            conn.commit();
            return true;
        } catch (Exception e) {
            logger.warning("Failed to mark notification " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            NotificationDAO dao = new NotificationDAO();
            Scanner scanner = new Scanner(System.in);
            switch (args[0]) {
                case "gets" -> {
                    int userId = scanner.nextInt();
                    ArrayList<Notification> notifications = dao.getNotifications(userId);
                    System.out.println(notifications.size());
                    for (Notification notification : notifications) {
                        System.out.println(notification.getContent());
                    }
                }
            }
        }
    }
}