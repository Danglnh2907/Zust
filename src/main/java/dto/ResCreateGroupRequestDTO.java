package dto;

import java.time.LocalDateTime;

public class ResCreateGroupRequestDTO {
    private int id;
    private String content;
    private LocalDateTime createDate;
    private String status;
    private String accountAvatar;
    private String accountName;

    public ResCreateGroupRequestDTO(int id, String content, LocalDateTime createDate, String status, String accountAvatar, String accountName) {
        this.id = id;
        this.content = content;
        this.createDate = createDate;
        this.status = status;
        this.accountAvatar = accountAvatar;
        this.accountName = accountName;
    }

    public int getId() {
        return id;
    }
    public String getContent() {
        return content;
    }
    public LocalDateTime getCreateDate() {
        return createDate;
    }
    public String getStatus() {
        return status;
    }
    public String getAccountAvatar() {
        return accountAvatar;
    }
    public String getAccountName() {
        return accountName;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setAccountAvatar(String accountAvatar) {
        this.accountAvatar = accountAvatar;
    }
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
