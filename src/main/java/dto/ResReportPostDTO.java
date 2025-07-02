package dto;
import model.Account;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResReportPostDTO {
    private int reportId;
    private List<String> reportContent;
    private Account account;
    private RespPostDTO post;
    private LocalDateTime reportCreateDate;
    private String reportStatus;

    // Constructor
    public ResReportPostDTO() {
    }

    public ResReportPostDTO(int reportId, String reportContent, Account account, RespPostDTO post,
                            LocalDateTime reportCreateDate, String reportStatus) {
        this.reportId = reportId;
        this.reportContent = extractContent(reportContent);
        this.account = account;
        this.post = post;
        this.reportCreateDate = reportCreateDate;
        this.reportStatus = reportStatus;
    }

    // Getters and Setters
    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
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
