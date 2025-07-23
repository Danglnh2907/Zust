package model;

import java.util.Date;

public class ResGroupReportCommentDTO {
    // Dùng để hiển thị
    private int reportId;
    private int commentId;
    private Account reporter;
    private Account commenter;
    private String commentContent;
    private String commentImage;
    private String reportMessage;
    private Date reportDate;

    // Dùng để xử lý accept
    private int reportAccountId;       // Người report
    private int reportedAccountId;     // Người bị report
    private String notificationContent;

    public ResGroupReportCommentDTO() {
        this.reporter = new Account();
        this.commenter = new Account();
    }

    // ===== Getters & Setters =====
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }

    public Account getReporter() { return reporter; }
    public void setReporter(Account reporter) { this.reporter = reporter; }

    public Account getCommenter() { return commenter; }
    public void setCommenter(Account commenter) { this.commenter = commenter; }

    public String getCommentContent() { return commentContent; }
    public void setCommentContent(String commentContent) { this.commentContent = commentContent; }

    public String getCommentImage() { return commentImage; }
    public void setCommentImage(String commentImage) { this.commentImage = commentImage; }

    public String getReportMessage() {
        return reportMessage != null ? reportMessage.replace("#", ", ") : "";
    }
    public void setReportMessage(String reportMessage) { this.reportMessage = reportMessage; }

    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }

    public int getReportAccountId() { return reportAccountId; }
    public void setReportAccountId(int reportAccountId) { this.reportAccountId = reportAccountId; }

    public int getReportedAccountId() { return reportedAccountId; }
    public void setReportedAccountId(int reportedAccountId) { this.reportedAccountId = reportedAccountId; }

    public String getNotificationContent() { return notificationContent; }
    public void setNotificationContent(String notificationContent) { this.notificationContent = notificationContent; }
}
