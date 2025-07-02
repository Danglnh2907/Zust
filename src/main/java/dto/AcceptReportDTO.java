package dto;

public class AcceptReportDTO {
    private int reportId;
    private int reportAccountId;
    private int reportedAccountId;
    private int reportedPostId;
    private String notificationContent;

    // Constructor
    public AcceptReportDTO() {
    }

    public AcceptReportDTO(int reportId, int reportAccountId, int reportedAccountId, int reportedPostId, String notificationContent) {
        this.reportId = reportId;
        this.reportAccountId = reportAccountId;
        this.reportedAccountId = reportedAccountId;
        this.reportedPostId = reportedPostId;
        this.notificationContent = notificationContent;
    }

    // Getters and Setters
    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getReportAccountId() {
        return reportAccountId;
    }

    public void setReportAccountId(int reportAccountId) {
        this.reportAccountId = reportAccountId;
    }

    public int getReportedAccountId() {
        return reportedAccountId;
    }

    public void setReportedAccountId(int reportedAccountId) {
        this.reportedAccountId = reportedAccountId;
    }

    public int getReportedPostId() {
        return reportedPostId;
    }

    public void setReportedPostId(int reportedPostId) {
        this.reportedPostId = reportedPostId;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(String notificationContent) {
        this.notificationContent = notificationContent;
    }
}
