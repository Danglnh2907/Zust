package dto;

import java.time.LocalDateTime;

public class ReqCommentDTO {
    private String content;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int accountID;
    private int postID;
    private int replyID;

    public ReqCommentDTO() {
        this.content = "";
        this.image = "";
        this.createdAt = null;
        this.updatedAt = null;
        this.accountID = -1;
        this.postID = -1;
        this.replyID = -1;
    }

    public ReqCommentDTO(int accountID, String content, LocalDateTime createdAt, String image, int postID, int replyID, LocalDateTime updatedAt) {
        this.accountID = accountID;
        this.content = content;
        this.createdAt = createdAt;
        this.image = image;
        this.postID = postID;
        this.replyID = replyID;
        this.updatedAt = updatedAt;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getPostID() {
        return postID;
    }

    public void setPostID(int postID) {
        this.postID = postID;
    }

    public int getReplyID() {
        return replyID;
    }

    public void setReplyID(int replyID) {
        this.replyID = replyID;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


}
