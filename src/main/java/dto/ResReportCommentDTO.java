package dto;

import model.Account;

import java.util.Date;

public class ResReportCommentDTO {
    private int id;
    private Account reporter;
    private Account commenter;
    private int commentId;
    private String commentContent;
    private String commentImage;
    private String reportMessage;
    private Date reportDate;

    public ResReportCommentDTO() {
        this.reporter = new Account();
        this.commenter = new Account();
        this.commentId = 0;
        this.commentContent = "";
        this.commentImage = "";
        this.reportMessage = "";
        this.reportDate = new Date();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Account getReporter() {
        return reporter;
    }

    public void setReporter(Account reporter) {
        if (reporter == null) {
            this.reporter = new Account();
        } else {
            this.reporter = reporter;
        }
    }

    public Account getCommenter() {
        return commenter;
    }

    public void setCommenter(Account commenter) {
        if (commenter == null) {
            this.commenter = new Account();
        } else {
            this.commenter = commenter;
        }
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent != null ? commentContent : "";
    }

    public String getCommentImage() {
        return commentImage;
    }

    public void setCommentImage(String commentImage) {
        this.commentImage = commentImage != null ? commentImage : "";
    }

    public String getReportMessage() {
        if (reportMessage == null) {
            return "";
        }
        // Replace # delimiter with <br> for display
        return reportMessage.replace("#", ", ");
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage != null ? reportMessage : "";
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        if (reportDate == null) {
            this.reportDate = new Date();
        } else {
            this.reportDate = reportDate;
        }
    }

    @Override
    public String toString() {
        return "ResReportCommentDTO{" +
                "id=" + id +
                ", reporter=" + (reporter != null ? "Account{id=" + reporter.getId() + ", username=" + reporter.getUsername() + "}" : "null") +
                ", commenter=" + (commenter != null ? "Account{id=" + commenter.getId() + ", username=" + commenter.getUsername() + "}" : "null") +
                ", commentId=" + commentId +
                ", commentContent='" + commentContent + '\'' +
                ", commentImage='" + commentImage + '\'' +
                ", reportMessage='" + reportMessage + '\'' +
                ", reportDate=" + reportDate +
                '}';
    }
}
