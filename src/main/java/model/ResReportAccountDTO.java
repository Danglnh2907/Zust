package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ResReportAccountDTO {
    private int reportId;
    private Account reporter;
    private Account reportedUser;
    private List<String> reportContent;
    private LocalDateTime reportDate;

    private final Logger logger = Logger.getLogger(ResReportAccountDTO.class.getName());

    public ResReportAccountDTO() {

    }

    // Getters and Setters
    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public Account getReporter() {
        return reporter;
    }

    public void setReporter(Account reporter) {
            this.reporter = reporter;
    }

    public Account getReportedUser() {
        return reportedUser;
    }

    public void setReportedUser(Account reportedUser) {
            this.reportedUser = reportedUser;
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

    public LocalDateTime getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDateTime reportDate) {
        this.reportDate = reportDate;
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
