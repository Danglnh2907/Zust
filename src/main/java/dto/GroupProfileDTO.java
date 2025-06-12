package dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupProfileDTO {
    private int groupId;
    private String groupName;
    private String description;
    private String avatarPath;
    private String status; // "public" hoặc "private"
    private int createdBy; // account_id
    private LocalDateTime groupCreateDate; // Thay createdAt
    private LocalDateTime lastUpdated;
    private List<String> errors;

    public GroupProfileDTO() {
        this.errors = new ArrayList<>();
    }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = "public".equalsIgnoreCase(status) ? "public" : "private";
    }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getGroupCreateDate() { return groupCreateDate; }
    public void setGroupCreateDate(LocalDateTime groupCreateDate) { this.groupCreateDate = groupCreateDate; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public List<String> getErrors() { return errors; }
    public void addError(String error) { this.errors.add(error); }
}