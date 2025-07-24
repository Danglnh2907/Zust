package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResReportCommentDTO {
    private int id;
    private Account reporter;
    private Account commenter;
    private int commentId;
    private String commentContent;
    private String commentImage;
    private List<String> reportContent;
    private Date reportDate;

    public ResReportCommentDTO() {
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
        this.reporter = reporter;
    }

    public Account getCommenter() {
        return commenter;
    }

    public void setCommenter(Account commenter) {
        this.commenter = commenter;
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
        this.commentContent = commentContent;
    }

    public String getCommentImage() {
        return commentImage;
    }

    public void setCommentImage(String commentImage) {
        this.commentImage = commentImage != null ? commentImage : "";
    }

    public String getReportContent() {
        String result = "";
        for (String content : reportContent) {
            result += content + "<br>";
        }
        return result;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = extractContent(reportContent);
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

    public static List<String> extractContent(String content){
        List<String> list = new ArrayList<>();
        String[] lines = content.split("#");
        for(String line : lines){
            line = line.trim();
            if(line.length() > 0){
                list.add(line);
            }
        }
        return list;
    }
}
