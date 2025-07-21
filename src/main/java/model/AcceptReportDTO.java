package model;

public class AcceptReportDTO {
    private int reportId;
    private int reportAccountId;
    private int reportedAccountId;
    private int reportedId;
    private String notificationContent;


    // Constructor
    public AcceptReportDTO() {
    }

    public AcceptReportDTO(int reportId, int reportAccountId, int reportedAccountId, int reportedId, String notificationContent) {
        this.reportId = reportId;
        this.reportAccountId = reportAccountId;
        this.reportedAccountId = reportedAccountId;
        this.reportedId = reportedId;
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

    public int getReportedId() {
        return reportedId;
    }

    public void setReportedId(int reportedId) {
        this.reportedId = reportedId;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(String notificationContent) {
        this.notificationContent = notificationContent;
    }
}
