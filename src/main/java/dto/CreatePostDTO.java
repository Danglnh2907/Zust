package dto;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * DTO object for packaging data for create post operation
 */
public class CreatePostDTO {
    private String postContent;
    private int accountID;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private String privacy;
    private String status;
    private int groupID;
    private final ArrayList<String> hashtags = new ArrayList<>();
    private final ArrayList<String> images = new ArrayList<>();

    public CreatePostDTO() {
        this.postContent = "";
        this.accountID = -1;
        this.createdAt = null;
        this.lastModified = null;
        this.privacy = "";
        this.status = "";
        this.groupID = -1;
    }

    public CreatePostDTO(int accountID, LocalDateTime createdAt, int groupID, LocalDateTime lastModified, String postContent, String privacy, String status) {
        this.accountID = accountID;
        this.createdAt = createdAt;
        this.groupID = groupID;
        this.lastModified = lastModified;
        this.postContent = postContent;
        this.privacy = privacy;
        this.status = status;
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public ArrayList<String> getHashtags() {
        return hashtags;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        privacy = privacy.toLowerCase().trim();
        if (!privacy.equals("public") && !privacy.equals("private") && !privacy.equals("friend")) {
            privacy = "public";
        }
        this.privacy = privacy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        status = status.toLowerCase().trim();
        if (!status.equals("drafted") && !status.equals("published") && !status.equals("rejected") && !status.equals("deleted")) {
            status = "published";
        }
        this.status = status;
    }
}
