package model;

import java.time.LocalDateTime;

public class ResGroupReportPostDTO {
    private int reportId;
    private String reportContent;
    private Account account;
    private RespPostDTO post;
    private LocalDateTime reportCreateDate;
    private String reportStatus;

    // Constructor mặc định
    public ResGroupReportPostDTO() {
    }

    // Constructor đầy đủ
    public ResGroupReportPostDTO(int reportId, String reportContent, Account account, RespPostDTO post,
                                 LocalDateTime reportCreateDate, String reportStatus) {
        this.reportId = reportId;
        this.reportContent = reportContent;
        this.account = account;
        this.post = post;
        this.reportCreateDate = reportCreateDate;
        this.reportStatus = reportStatus;
    }

    // Getters và Setters
    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public RespPostDTO getPost() {
        return post;
    }

    public void setPost(RespPostDTO post) {
        this.post = post;
    }

    public LocalDateTime getReportCreateDate() {
        return reportCreateDate;
    }

    public void setReportCreateDate(LocalDateTime reportCreateDate) {
        this.reportCreateDate = reportCreateDate;
    }

    public String getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(String reportStatus) {
        this.reportStatus = reportStatus;
    }
}