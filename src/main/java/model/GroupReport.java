package model;

public class GroupReport {
    private RespPostDTO reportedPost;
    private RespCommentDTO reportedComment;
    private int reporterID;
    private String reporterUsername;
    private String reporterAvatar;
    private int reportID;
    private String reportContent;

    public GroupReport() {}

    public RespCommentDTO getReportedComment() {
        return reportedComment;
    }

    public void setReportedComment(RespCommentDTO reportedComment) {
        this.reportedComment = reportedComment;
    }

    public RespPostDTO getReportedPost() {
        return reportedPost;
    }

    public void setReportedPost(RespPostDTO reportedPost) {
        this.reportedPost = reportedPost;
    }

    public int getReporterID() {
        return reporterID;
    }

    public void setReporterID(int reporterID) {
        this.reporterID = reporterID;
    }

    public int getReportID() {
        return reportID;
    }

    public void setReportID(int reportID) {
        this.reportID = reportID;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }

    public String getReporterAvatar() {
        return reporterAvatar;
    }

    public void setReporterAvatar(String reporterAvatar) {
        this.reporterAvatar = reporterAvatar;
    }

    public String getReporterUsername() {
        return reporterUsername;
    }

    public void setReporterUsername(String reporterUsername) {
        this.reporterUsername = reporterUsername;
    }
}
